package com.veritas.web.rest;

import static com.veritas.domain.FileVersionAsserts.*;
import static com.veritas.web.rest.TestUtil.createUpdateProxyForBean;
import static com.veritas.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veritas.IntegrationTest;
import com.veritas.domain.FileVersion;
import com.veritas.repository.FileVersionRepository;
import com.veritas.repository.search.FileVersionSearchRepository;
import com.veritas.service.dto.FileVersionDTO;
import com.veritas.service.mapper.FileVersionMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link FileVersionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class FileVersionResourceIT {

    private static final String DEFAULT_FILE_ID = "AAAAAAAAAA";
    private static final String UPDATED_FILE_ID = "BBBBBBBBBB";

    private static final Integer DEFAULT_VERSION_NUMBER = 1;
    private static final Integer UPDATED_VERSION_NUMBER = 2;

    private static final String DEFAULT_OBJECT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_OBJECT_NAME = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/file-versions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/file-versions/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FileVersionRepository fileVersionRepository;

    @Autowired
    private FileVersionMapper fileVersionMapper;

    @Autowired
    private FileVersionSearchRepository fileVersionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFileVersionMockMvc;

    private FileVersion fileVersion;

    private FileVersion insertedFileVersion;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FileVersion createEntity(EntityManager em) {
        FileVersion fileVersion = new FileVersion()
            .fileId(DEFAULT_FILE_ID)
            .versionNumber(DEFAULT_VERSION_NUMBER)
            .objectName(DEFAULT_OBJECT_NAME)
            .createdAt(DEFAULT_CREATED_AT);
        return fileVersion;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FileVersion createUpdatedEntity(EntityManager em) {
        FileVersion fileVersion = new FileVersion()
            .fileId(UPDATED_FILE_ID)
            .versionNumber(UPDATED_VERSION_NUMBER)
            .objectName(UPDATED_OBJECT_NAME)
            .createdAt(UPDATED_CREATED_AT);
        return fileVersion;
    }

    @BeforeEach
    public void initTest() {
        fileVersion = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedFileVersion != null) {
            fileVersionRepository.delete(insertedFileVersion);
            fileVersionSearchRepository.delete(insertedFileVersion);
            insertedFileVersion = null;
        }
    }

    @Test
    @Transactional
    void createFileVersion() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        // Create the FileVersion
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);
        var returnedFileVersionDTO = om.readValue(
            restFileVersionMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileVersionDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FileVersionDTO.class
        );

        // Validate the FileVersion in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFileVersion = fileVersionMapper.toEntity(returnedFileVersionDTO);
        assertFileVersionUpdatableFieldsEquals(returnedFileVersion, getPersistedFileVersion(returnedFileVersion));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedFileVersion = returnedFileVersion;
    }

    @Test
    @Transactional
    void createFileVersionWithExistingId() throws Exception {
        // Create the FileVersion with an existing ID
        fileVersion.setId(1L);
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restFileVersionMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FileVersion in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkFileIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        // set the field null
        fileVersion.setFileId(null);

        // Create the FileVersion, which fails.
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        restFileVersionMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkVersionNumberIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        // set the field null
        fileVersion.setVersionNumber(null);

        // Create the FileVersion, which fails.
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        restFileVersionMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkObjectNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        // set the field null
        fileVersion.setObjectName(null);

        // Create the FileVersion, which fails.
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        restFileVersionMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllFileVersions() throws Exception {
        // Initialize the database
        insertedFileVersion = fileVersionRepository.saveAndFlush(fileVersion);

        // Get all the fileVersionList
        restFileVersionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fileVersion.getId().intValue())))
            .andExpect(jsonPath("$.[*].fileId").value(hasItem(DEFAULT_FILE_ID)))
            .andExpect(jsonPath("$.[*].versionNumber").value(hasItem(DEFAULT_VERSION_NUMBER)))
            .andExpect(jsonPath("$.[*].objectName").value(hasItem(DEFAULT_OBJECT_NAME)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))));
    }

    @Test
    @Transactional
    void getFileVersion() throws Exception {
        // Initialize the database
        insertedFileVersion = fileVersionRepository.saveAndFlush(fileVersion);

        // Get the fileVersion
        restFileVersionMockMvc
            .perform(get(ENTITY_API_URL_ID, fileVersion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(fileVersion.getId().intValue()))
            .andExpect(jsonPath("$.fileId").value(DEFAULT_FILE_ID))
            .andExpect(jsonPath("$.versionNumber").value(DEFAULT_VERSION_NUMBER))
            .andExpect(jsonPath("$.objectName").value(DEFAULT_OBJECT_NAME))
            .andExpect(jsonPath("$.createdAt").value(sameInstant(DEFAULT_CREATED_AT)));
    }

    @Test
    @Transactional
    void getNonExistingFileVersion() throws Exception {
        // Get the fileVersion
        restFileVersionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFileVersion() throws Exception {
        // Initialize the database
        insertedFileVersion = fileVersionRepository.saveAndFlush(fileVersion);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        fileVersionSearchRepository.save(fileVersion);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());

        // Update the fileVersion
        FileVersion updatedFileVersion = fileVersionRepository.findById(fileVersion.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFileVersion are not directly saved in db
        em.detach(updatedFileVersion);
        updatedFileVersion
            .fileId(UPDATED_FILE_ID)
            .versionNumber(UPDATED_VERSION_NUMBER)
            .objectName(UPDATED_OBJECT_NAME)
            .createdAt(UPDATED_CREATED_AT);
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(updatedFileVersion);

        restFileVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fileVersionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isOk());

        // Validate the FileVersion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFileVersionToMatchAllProperties(updatedFileVersion);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<FileVersion> fileVersionSearchList = Streamable.of(fileVersionSearchRepository.findAll()).toList();
                FileVersion testFileVersionSearch = fileVersionSearchList.get(searchDatabaseSizeAfter - 1);

                assertFileVersionAllPropertiesEquals(testFileVersionSearch, updatedFileVersion);
            });
    }

    @Test
    @Transactional
    void putNonExistingFileVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        fileVersion.setId(longCount.incrementAndGet());

        // Create the FileVersion
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFileVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fileVersionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FileVersion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchFileVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        fileVersion.setId(longCount.incrementAndGet());

        // Create the FileVersion
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFileVersionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FileVersion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFileVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        fileVersion.setId(longCount.incrementAndGet());

        // Create the FileVersion
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFileVersionMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileVersionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the FileVersion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateFileVersionWithPatch() throws Exception {
        // Initialize the database
        insertedFileVersion = fileVersionRepository.saveAndFlush(fileVersion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the fileVersion using partial update
        FileVersion partialUpdatedFileVersion = new FileVersion();
        partialUpdatedFileVersion.setId(fileVersion.getId());

        partialUpdatedFileVersion.objectName(UPDATED_OBJECT_NAME).createdAt(UPDATED_CREATED_AT);

        restFileVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFileVersion.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFileVersion))
            )
            .andExpect(status().isOk());

        // Validate the FileVersion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFileVersionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedFileVersion, fileVersion),
            getPersistedFileVersion(fileVersion)
        );
    }

    @Test
    @Transactional
    void fullUpdateFileVersionWithPatch() throws Exception {
        // Initialize the database
        insertedFileVersion = fileVersionRepository.saveAndFlush(fileVersion);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the fileVersion using partial update
        FileVersion partialUpdatedFileVersion = new FileVersion();
        partialUpdatedFileVersion.setId(fileVersion.getId());

        partialUpdatedFileVersion
            .fileId(UPDATED_FILE_ID)
            .versionNumber(UPDATED_VERSION_NUMBER)
            .objectName(UPDATED_OBJECT_NAME)
            .createdAt(UPDATED_CREATED_AT);

        restFileVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFileVersion.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFileVersion))
            )
            .andExpect(status().isOk());

        // Validate the FileVersion in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFileVersionUpdatableFieldsEquals(partialUpdatedFileVersion, getPersistedFileVersion(partialUpdatedFileVersion));
    }

    @Test
    @Transactional
    void patchNonExistingFileVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        fileVersion.setId(longCount.incrementAndGet());

        // Create the FileVersion
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFileVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, fileVersionDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FileVersion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFileVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        fileVersion.setId(longCount.incrementAndGet());

        // Create the FileVersion
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFileVersionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the FileVersion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFileVersion() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        fileVersion.setId(longCount.incrementAndGet());

        // Create the FileVersion
        FileVersionDTO fileVersionDTO = fileVersionMapper.toDto(fileVersion);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFileVersionMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(fileVersionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the FileVersion in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteFileVersion() throws Exception {
        // Initialize the database
        insertedFileVersion = fileVersionRepository.saveAndFlush(fileVersion);
        fileVersionRepository.save(fileVersion);
        fileVersionSearchRepository.save(fileVersion);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the fileVersion
        restFileVersionMockMvc
            .perform(delete(ENTITY_API_URL_ID, fileVersion.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileVersionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchFileVersion() throws Exception {
        // Initialize the database
        insertedFileVersion = fileVersionRepository.saveAndFlush(fileVersion);
        fileVersionSearchRepository.save(fileVersion);

        // Search the fileVersion
        restFileVersionMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + fileVersion.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fileVersion.getId().intValue())))
            .andExpect(jsonPath("$.[*].fileId").value(hasItem(DEFAULT_FILE_ID)))
            .andExpect(jsonPath("$.[*].versionNumber").value(hasItem(DEFAULT_VERSION_NUMBER)))
            .andExpect(jsonPath("$.[*].objectName").value(hasItem(DEFAULT_OBJECT_NAME)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))));
    }

    protected long getRepositoryCount() {
        return fileVersionRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected FileVersion getPersistedFileVersion(FileVersion fileVersion) {
        return fileVersionRepository.findById(fileVersion.getId()).orElseThrow();
    }

    protected void assertPersistedFileVersionToMatchAllProperties(FileVersion expectedFileVersion) {
        assertFileVersionAllPropertiesEquals(expectedFileVersion, getPersistedFileVersion(expectedFileVersion));
    }

    protected void assertPersistedFileVersionToMatchUpdatableProperties(FileVersion expectedFileVersion) {
        assertFileVersionAllUpdatablePropertiesEquals(expectedFileVersion, getPersistedFileVersion(expectedFileVersion));
    }
}
