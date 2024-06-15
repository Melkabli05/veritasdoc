package com.veritas.web.rest;

import static com.veritas.domain.FolderAsserts.*;
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
import com.veritas.domain.Folder;
import com.veritas.repository.FolderRepository;
import com.veritas.repository.search.FolderSearchRepository;
import com.veritas.service.dto.FolderDTO;
import com.veritas.service.mapper.FolderMapper;
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
 * Integration tests for the {@link FolderResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class FolderResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_PARENT_FOLDER_ID = "AAAAAAAAAA";
    private static final String UPDATED_PARENT_FOLDER_ID = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final Boolean DEFAULT_IS_ACTIVE = false;
    private static final Boolean UPDATED_IS_ACTIVE = true;

    private static final String ENTITY_API_URL = "/api/folders";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/folders/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderMapper folderMapper;

    @Autowired
    private FolderSearchRepository folderSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFolderMockMvc;

    private Folder folder;

    private Folder insertedFolder;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Folder createEntity(EntityManager em) {
        Folder folder = new Folder()
            .name(DEFAULT_NAME)
            .parentFolderId(DEFAULT_PARENT_FOLDER_ID)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT)
            .isActive(DEFAULT_IS_ACTIVE);
        return folder;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Folder createUpdatedEntity(EntityManager em) {
        Folder folder = new Folder()
            .name(UPDATED_NAME)
            .parentFolderId(UPDATED_PARENT_FOLDER_ID)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .isActive(UPDATED_IS_ACTIVE);
        return folder;
    }

    @BeforeEach
    public void initTest() {
        folder = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedFolder != null) {
            folderRepository.delete(insertedFolder);
            folderSearchRepository.delete(insertedFolder);
            insertedFolder = null;
        }
    }

    @Test
    @Transactional
    void createFolder() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        // Create the Folder
        FolderDTO folderDTO = folderMapper.toDto(folder);
        var returnedFolderDTO = om.readValue(
            restFolderMockMvc
                .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(folderDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            FolderDTO.class
        );

        // Validate the Folder in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedFolder = folderMapper.toEntity(returnedFolderDTO);
        assertFolderUpdatableFieldsEquals(returnedFolder, getPersistedFolder(returnedFolder));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedFolder = returnedFolder;
    }

    @Test
    @Transactional
    void createFolderWithExistingId() throws Exception {
        // Create the Folder with an existing ID
        folder.setId(1L);
        FolderDTO folderDTO = folderMapper.toDto(folder);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restFolderMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(folderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Folder in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        // set the field null
        folder.setName(null);

        // Create the Folder, which fails.
        FolderDTO folderDTO = folderMapper.toDto(folder);

        restFolderMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(folderDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllFolders() throws Exception {
        // Initialize the database
        insertedFolder = folderRepository.saveAndFlush(folder);

        // Get all the folderList
        restFolderMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(folder.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].parentFolderId").value(hasItem(DEFAULT_PARENT_FOLDER_ID)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE.booleanValue())));
    }

    @Test
    @Transactional
    void getFolder() throws Exception {
        // Initialize the database
        insertedFolder = folderRepository.saveAndFlush(folder);

        // Get the folder
        restFolderMockMvc
            .perform(get(ENTITY_API_URL_ID, folder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(folder.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.parentFolderId").value(DEFAULT_PARENT_FOLDER_ID))
            .andExpect(jsonPath("$.createdAt").value(sameInstant(DEFAULT_CREATED_AT)))
            .andExpect(jsonPath("$.updatedAt").value(sameInstant(DEFAULT_UPDATED_AT)))
            .andExpect(jsonPath("$.isActive").value(DEFAULT_IS_ACTIVE.booleanValue()));
    }

    @Test
    @Transactional
    void getNonExistingFolder() throws Exception {
        // Get the folder
        restFolderMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingFolder() throws Exception {
        // Initialize the database
        insertedFolder = folderRepository.saveAndFlush(folder);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        folderSearchRepository.save(folder);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());

        // Update the folder
        Folder updatedFolder = folderRepository.findById(folder.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedFolder are not directly saved in db
        em.detach(updatedFolder);
        updatedFolder
            .name(UPDATED_NAME)
            .parentFolderId(UPDATED_PARENT_FOLDER_ID)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .isActive(UPDATED_IS_ACTIVE);
        FolderDTO folderDTO = folderMapper.toDto(updatedFolder);

        restFolderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, folderDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(folderDTO))
            )
            .andExpect(status().isOk());

        // Validate the Folder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedFolderToMatchAllProperties(updatedFolder);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Folder> folderSearchList = Streamable.of(folderSearchRepository.findAll()).toList();
                Folder testFolderSearch = folderSearchList.get(searchDatabaseSizeAfter - 1);

                assertFolderAllPropertiesEquals(testFolderSearch, updatedFolder);
            });
    }

    @Test
    @Transactional
    void putNonExistingFolder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        folder.setId(longCount.incrementAndGet());

        // Create the Folder
        FolderDTO folderDTO = folderMapper.toDto(folder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFolderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, folderDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(folderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Folder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchFolder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        folder.setId(longCount.incrementAndGet());

        // Create the Folder
        FolderDTO folderDTO = folderMapper.toDto(folder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFolderMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(folderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Folder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamFolder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        folder.setId(longCount.incrementAndGet());

        // Create the Folder
        FolderDTO folderDTO = folderMapper.toDto(folder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFolderMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(folderDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Folder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateFolderWithPatch() throws Exception {
        // Initialize the database
        insertedFolder = folderRepository.saveAndFlush(folder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the folder using partial update
        Folder partialUpdatedFolder = new Folder();
        partialUpdatedFolder.setId(folder.getId());

        partialUpdatedFolder
            .name(UPDATED_NAME)
            .parentFolderId(UPDATED_PARENT_FOLDER_ID)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restFolderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFolder.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFolder))
            )
            .andExpect(status().isOk());

        // Validate the Folder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFolderUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedFolder, folder), getPersistedFolder(folder));
    }

    @Test
    @Transactional
    void fullUpdateFolderWithPatch() throws Exception {
        // Initialize the database
        insertedFolder = folderRepository.saveAndFlush(folder);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the folder using partial update
        Folder partialUpdatedFolder = new Folder();
        partialUpdatedFolder.setId(folder.getId());

        partialUpdatedFolder
            .name(UPDATED_NAME)
            .parentFolderId(UPDATED_PARENT_FOLDER_ID)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT)
            .isActive(UPDATED_IS_ACTIVE);

        restFolderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedFolder.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedFolder))
            )
            .andExpect(status().isOk());

        // Validate the Folder in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertFolderUpdatableFieldsEquals(partialUpdatedFolder, getPersistedFolder(partialUpdatedFolder));
    }

    @Test
    @Transactional
    void patchNonExistingFolder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        folder.setId(longCount.incrementAndGet());

        // Create the Folder
        FolderDTO folderDTO = folderMapper.toDto(folder);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restFolderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, folderDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(folderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Folder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchFolder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        folder.setId(longCount.incrementAndGet());

        // Create the Folder
        FolderDTO folderDTO = folderMapper.toDto(folder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFolderMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(folderDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Folder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamFolder() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        folder.setId(longCount.incrementAndGet());

        // Create the Folder
        FolderDTO folderDTO = folderMapper.toDto(folder);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restFolderMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(folderDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Folder in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteFolder() throws Exception {
        // Initialize the database
        insertedFolder = folderRepository.saveAndFlush(folder);
        folderRepository.save(folder);
        folderSearchRepository.save(folder);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the folder
        restFolderMockMvc
            .perform(delete(ENTITY_API_URL_ID, folder.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(folderSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchFolder() throws Exception {
        // Initialize the database
        insertedFolder = folderRepository.saveAndFlush(folder);
        folderSearchRepository.save(folder);

        // Search the folder
        restFolderMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + folder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(folder.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].parentFolderId").value(hasItem(DEFAULT_PARENT_FOLDER_ID)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))))
            .andExpect(jsonPath("$.[*].isActive").value(hasItem(DEFAULT_IS_ACTIVE.booleanValue())));
    }

    protected long getRepositoryCount() {
        return folderRepository.count();
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

    protected Folder getPersistedFolder(Folder folder) {
        return folderRepository.findById(folder.getId()).orElseThrow();
    }

    protected void assertPersistedFolderToMatchAllProperties(Folder expectedFolder) {
        assertFolderAllPropertiesEquals(expectedFolder, getPersistedFolder(expectedFolder));
    }

    protected void assertPersistedFolderToMatchUpdatableProperties(Folder expectedFolder) {
        assertFolderAllUpdatablePropertiesEquals(expectedFolder, getPersistedFolder(expectedFolder));
    }
}
