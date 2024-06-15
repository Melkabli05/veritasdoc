package com.veritas.web.rest;

import static com.veritas.domain.FileAsserts.*;
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
import com.veritas.domain.File;
import com.veritas.repository.FileRepository;
import com.veritas.repository.search.FileSearchRepository;
import com.veritas.service.FileService;
import com.veritas.service.dto.FileDTO;
import com.veritas.service.mapper.FileMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link FileResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class FileResourceIT {

    private static final String DEFAULT_FILENAME = "AAAAAAAAAA";
    private static final String UPDATED_FILENAME = "BBBBBBBBBB";

    private static final String DEFAULT_BUCKET_NAME = "AAAAAAAAAA";
    private static final String UPDATED_BUCKET_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_OBJECT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_OBJECT_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_CONTENT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_CONTENT_TYPE = "BBBBBBBBBB";

    private static final Long DEFAULT_FILE_SIZE = 1L;
    private static final Long UPDATED_FILE_SIZE = 2L;

    private static final String DEFAULT_UPLOADED_BY = "AAAAAAAAAA";
    private static final String UPDATED_UPLOADED_BY = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/files";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/files/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FileRepository fileRepository;

    @Mock
    private FileRepository fileRepositoryMock;

    @Autowired
    private FileMapper fileMapper;

    @Mock
    private FileService fileServiceMock;

    @Autowired
    private FileSearchRepository fileSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFileMockMvc;

    private File file;

    private File insertedFile;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static File createEntity(EntityManager em) {
        File file = new File()
            .filename(DEFAULT_FILENAME)
            .bucketName(DEFAULT_BUCKET_NAME)
            .objectName(DEFAULT_OBJECT_NAME)
            .contentType(DEFAULT_CONTENT_TYPE)
            .fileSize(DEFAULT_FILE_SIZE)
            .uploadedBy(DEFAULT_UPLOADED_BY)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .isActive(DEFAULT_IS_ACTIVE);
        return file;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static File createUpdatedEntity(EntityManager em) {
        File file = new File()
            .filename(UPDATED_FILENAME)
            .bucketName(UPDATED_BUCKET_NAME)
            .objectName(UPDATED_OBJECT_NAME)
            .contentType(UPDATED_CONTENT_TYPE)
            .fileSize(UPDATED_FILE_SIZE)
            .uploadedBy(UPDATED_UPLOADED_BY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .isActive(UPDATED_IS_ACTIVE);
        return file;
    }

    @BeforeEach
    public void initTest() {
        file = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedFile != null) {
            fileRepository.delete(insertedFile);
            fileSearchRepository.delete(insertedFile);
            insertedFile = null;
        }
    }

    @Test
    @Transactional
    void createFile() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        // Create the File
        FileDTO fileDTO = fileMapper.toDto(file);
        var returnedFileDTO = om.readValue(
            restFileMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FileDTO.class
        );

        // Validate the File in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFile = fileMapper.toEntity(returnedFileDTO);
        assertFileUpdatableFieldsEquals(returnedFile, getPersistedFile(returnedFile));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedFile = returnedFile;
    }

    @Test
    @Transactional
    void createFileWithExistingId() throws Exception {
        // Create the File with an existing ID
        file.setId(1L);
        FileDTO fileDTO = fileMapper.toDto(file);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restFileMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileDTO)))
            .andExpect(status().isBadRequest());

        // Validate the File in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkFilenameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        // set the field null
        file.setFilename(null);

        // Create the File, which fails.
        FileDTO fileDTO = fileMapper.toDto(file);

        restFileMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkBucketNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        // set the field null
        file.setBucketName(null);

        // Create the File, which fails.
        FileDTO fileDTO = fileMapper.toDto(file);

        restFileMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkObjectNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        // set the field null
        file.setObjectName(null);

        // Create the File, which fails.
        FileDTO fileDTO = fileMapper.toDto(file);

        restFileMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllFiles() throws Exception {
        // Initialize the database
        insertedFile = fileRepository.saveAndFlush(file);

        // Get all the fileList
        restFileMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(file.getId().intValue())))
            .andExpect(jsonPath("$.[*].filename").value(hasItem(DEFAULT_FILENAME)))
            .andExpect(jsonPath("$.[*].bucketName").value(hasItem(DEFAULT_BUCKET_NAME)))
            .andExpect(jsonPath("$.[*].objectName").value(hasItem(DEFAULT_OBJECT_NAME)))
            .andExpect(jsonPath("$.[*].contentType").value(hasItem(DEFAULT_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].fileSize").value(hasItem(DEFAULT_FILE_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].uploadedBy").value(hasItem(DEFAULT_UPLOADED_BY)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE.booleanValue())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFilesWithEagerRelationshipsIsEnabled() throws Exception {
        when(fileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(fileServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllFilesWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(fileServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restFileMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(fileRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getFile() throws Exception {
        // Initialize the database
        insertedFile = fileRepository.saveAndFlush(file);

        // Get the file
        restFileMockMvc
            .perform(get(ENTITY_API_URL_ID, file.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(file.getId().intValue()))
            .andExpect(jsonPath("$.filename").value(DEFAULT_FILENAME))
            .andExpect(jsonPath("$.bucketName").value(DEFAULT_BUCKET_NAME))
            .andExpect(jsonPath("$.objectName").value(DEFAULT_OBJECT_NAME))
            .andExpect(jsonPath("$.contentType").value(DEFAULT_CONTENT_TYPE))
            .andExpect(jsonPath("$.fileSize").value(DEFAULT_FILE_SIZE.intValue()))
            .andExpect(jsonPath("$.uploadedBy").value(DEFAULT_UPLOADED_BY))
            .andExpect(jsonPath("$.createdAt").value(sameInstant(DEFAULT_CREATED_AT)))
            .andExpect(jsonPath("$.updatedAt").value(sameInstant(DEFAULT_UPDATED_AT)))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    void getNonExistingFile() throws Exception {
        // Get the file
        restFileMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFile() throws Exception {
        // Initialize the database
        insertedFile = fileRepository.saveAndFlush(file);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        fileSearchRepository.save(file);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());

        // Update the file
        File updatedFile = fileRepository.findById(file.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFile are not directly saved in db
        em.detach(updatedFile);
        updatedFile
            .filename(UPDATED_FILENAME)
            .bucketName(UPDATED_BUCKET_NAME)
            .objectName(UPDATED_OBJECT_NAME)
            .contentType(UPDATED_CONTENT_TYPE)
            .fileSize(UPDATED_FILE_SIZE)
            .uploadedBy(UPDATED_UPLOADED_BY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .isActive(UPDATED_IS_ACTIVE);
        FileDTO fileDTO = fileMapper.toDto(updatedFile);

        restFileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fileDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fileDTO))
            )
            .andExpect(status().isOk());

        // Validate the File in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFileToMatchAllProperties(updatedFile);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<File> fileSearchList = Streamable.of(fileSearchRepository.findAll()).toList();
                File testFileSearch = fileSearchList.get(searchDatabaseSizeAfter - 1);

                assertFileAllPropertiesEquals(testFileSearch, updatedFile);
            });
    }

    @Test
    @Transactional
    void putNonExistingFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        file.setId(longCount.incrementAndGet());

        // Create the File
        FileDTO fileDTO = fileMapper.toDto(file);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, fileDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the File in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        file.setId(longCount.incrementAndGet());

        // Create the File
        FileDTO fileDTO = fileMapper.toDto(file);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFileMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(fileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the File in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        file.setId(longCount.incrementAndGet());

        // Create the File
        FileDTO fileDTO = fileMapper.toDto(file);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFileMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(fileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the File in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateFileWithPatch() throws Exception {
        // Initialize the database
        insertedFile = fileRepository.saveAndFlush(file);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the file using partial update
        File partialUpdatedFile = new File();
        partialUpdatedFile.setId(file.getId());

        partialUpdatedFile
            .filename(UPDATED_FILENAME)
            .objectName(UPDATED_OBJECT_NAME)
            .updatedAt(UPDATED_UPDATED_AT)
            .isActive(UPDATED_IS_ACTIVE);

        restFileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFile.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFile))
            )
            .andExpect(status().isOk());

        // Validate the File in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFileUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedFile, file), getPersistedFile(file));
    }

    @Test
    @Transactional
    void fullUpdateFileWithPatch() throws Exception {
        // Initialize the database
        insertedFile = fileRepository.saveAndFlush(file);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the file using partial update
        File partialUpdatedFile = new File();
        partialUpdatedFile.setId(file.getId());

        partialUpdatedFile
            .filename(UPDATED_FILENAME)
            .bucketName(UPDATED_BUCKET_NAME)
            .objectName(UPDATED_OBJECT_NAME)
            .contentType(UPDATED_CONTENT_TYPE)
            .fileSize(UPDATED_FILE_SIZE)
            .uploadedBy(UPDATED_UPLOADED_BY)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .isActive(UPDATED_IS_ACTIVE);

        restFileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFile.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFile))
            )
            .andExpect(status().isOk());

        // Validate the File in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFileUpdatableFieldsEquals(partialUpdatedFile, getPersistedFile(partialUpdatedFile));
    }

    @Test
    @Transactional
    void patchNonExistingFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        file.setId(longCount.incrementAndGet());

        // Create the File
        FileDTO fileDTO = fileMapper.toDto(file);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, fileDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(fileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the File in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        file.setId(longCount.incrementAndGet());

        // Create the File
        FileDTO fileDTO = fileMapper.toDto(file);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFileMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(fileDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the File in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFile() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        file.setId(longCount.incrementAndGet());

        // Create the File
        FileDTO fileDTO = fileMapper.toDto(file);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFileMockMvc
            .perform(patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(fileDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the File in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteFile() throws Exception {
        // Initialize the database
        insertedFile = fileRepository.saveAndFlush(file);
        fileRepository.save(file);
        fileSearchRepository.save(file);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the file
        restFileMockMvc
            .perform(delete(ENTITY_API_URL_ID, file.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(fileSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchFile() throws Exception {
        // Initialize the database
        insertedFile = fileRepository.saveAndFlush(file);
        fileSearchRepository.save(file);

        // Search the file
        restFileMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + file.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(file.getId().intValue())))
            .andExpect(jsonPath("$.[*].filename").value(hasItem(DEFAULT_FILENAME)))
            .andExpect(jsonPath("$.[*].bucketName").value(hasItem(DEFAULT_BUCKET_NAME)))
            .andExpect(jsonPath("$.[*].objectName").value(hasItem(DEFAULT_OBJECT_NAME)))
            .andExpect(jsonPath("$.[*].contentType").value(hasItem(DEFAULT_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].fileSize").value(hasItem(DEFAULT_FILE_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].uploadedBy").value(hasItem(DEFAULT_UPLOADED_BY)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE.booleanValue())));
    }

    protected long getRepositoryCount() {
        return fileRepository.count();
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

    protected File getPersistedFile(File file) {
        return fileRepository.findById(file.getId()).orElseThrow();
    }

    protected void assertPersistedFileToMatchAllProperties(File expectedFile) {
        assertFileAllPropertiesEquals(expectedFile, getPersistedFile(expectedFile));
    }

    protected void assertPersistedFileToMatchUpdatableProperties(File expectedFile) {
        assertFileAllUpdatablePropertiesEquals(expectedFile, getPersistedFile(expectedFile));
    }
}
