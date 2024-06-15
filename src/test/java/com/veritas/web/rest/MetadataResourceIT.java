package com.veritas.web.rest;

import static com.veritas.domain.MetadataAsserts.*;
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
import com.veritas.domain.Metadata;
import com.veritas.repository.MetadataRepository;
import com.veritas.repository.search.MetadataSearchRepository;
import com.veritas.service.dto.MetadataDTO;
import com.veritas.service.mapper.MetadataMapper;
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
 * Integration tests for the {@link MetadataResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class MetadataResourceIT {

    private static final String DEFAULT_FILE_ID = "AAAAAAAAAA";
    private static final String UPDATED_FILE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_KEY = "AAAAAAAAAA";
    private static final String UPDATED_KEY = "BBBBBBBBBB";

    private static final String DEFAULT_VALUE = "AAAAAAAAAA";
    private static final String UPDATED_VALUE = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/metadata";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/metadata/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private MetadataMapper metadataMapper;

    @Autowired
    private MetadataSearchRepository metadataSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restMetadataMockMvc;

    private Metadata metadata;

    private Metadata insertedMetadata;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Metadata createEntity(EntityManager em) {
        Metadata metadata = new Metadata()
            .fileId(DEFAULT_FILE_ID)
            .key(DEFAULT_KEY)
            .value(DEFAULT_VALUE)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
        return metadata;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Metadata createUpdatedEntity(EntityManager em) {
        Metadata metadata = new Metadata()
            .fileId(UPDATED_FILE_ID)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        return metadata;
    }

    @BeforeEach
    public void initTest() {
        metadata = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedMetadata != null) {
            metadataRepository.delete(insertedMetadata);
            metadataSearchRepository.delete(insertedMetadata);
            insertedMetadata = null;
        }
    }

    @Test
    @Transactional
    void createMetadata() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        // Create the Metadata
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);
        var returnedMetadataDTO = om.readValue(
            restMetadataMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metadataDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            MetadataDTO.class
        );

        // Validate the Metadata in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedMetadata = metadataMapper.toEntity(returnedMetadataDTO);
        assertMetadataUpdatableFieldsEquals(returnedMetadata, getPersistedMetadata(returnedMetadata));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedMetadata = returnedMetadata;
    }

    @Test
    @Transactional
    void createMetadataWithExistingId() throws Exception {
        // Create the Metadata with an existing ID
        metadata.setId(1L);
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restMetadataMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metadataDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Metadata in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkFileIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        // set the field null
        metadata.setFileId(null);

        // Create the Metadata, which fails.
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        restMetadataMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metadataDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkKeyIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        // set the field null
        metadata.setKey(null);

        // Create the Metadata, which fails.
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        restMetadataMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metadataDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkValueIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        // set the field null
        metadata.setValue(null);

        // Create the Metadata, which fails.
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        restMetadataMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metadataDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllMetadata() throws Exception {
        // Initialize the database
        insertedMetadata = metadataRepository.saveAndFlush(metadata);

        // Get all the metadataList
        restMetadataMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(metadata.getId().intValue())))
            .andExpect(jsonPath("$.[*].fileId").value(hasItem(DEFAULT_FILE_ID)))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))));
    }

    @Test
    @Transactional
    void getMetadata() throws Exception {
        // Initialize the database
        insertedMetadata = metadataRepository.saveAndFlush(metadata);

        // Get the metadata
        restMetadataMockMvc
            .perform(get(ENTITY_API_URL_ID, metadata.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(metadata.getId().intValue()))
            .andExpect(jsonPath("$.fileId").value(DEFAULT_FILE_ID))
            .andExpect(jsonPath("$.key").value(DEFAULT_KEY))
            .andExpect(jsonPath("$.value").value(DEFAULT_VALUE))
            .andExpect(jsonPath("$.createdAt").value(sameInstant(DEFAULT_CREATED_AT)))
            .andExpect(jsonPath("$.updatedAt").value(sameInstant(DEFAULT_UPDATED_AT)));
    }

    @Test
    @Transactional
    void getNonExistingMetadata() throws Exception {
        // Get the metadata
        restMetadataMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingMetadata() throws Exception {
        // Initialize the database
        insertedMetadata = metadataRepository.saveAndFlush(metadata);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        metadataSearchRepository.save(metadata);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());

        // Update the metadata
        Metadata updatedMetadata = metadataRepository.findById(metadata.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedMetadata are not directly saved in db
        em.detach(updatedMetadata);
        updatedMetadata
            .fileId(UPDATED_FILE_ID)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        MetadataDTO metadataDTO = metadataMapper.toDto(updatedMetadata);

        restMetadataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, metadataDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metadataDTO))
            )
            .andExpect(status().isOk());

        // Validate the Metadata in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedMetadataToMatchAllProperties(updatedMetadata);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Metadata> metadataSearchList = Streamable.of(metadataSearchRepository.findAll()).toList();
                Metadata testMetadataSearch = metadataSearchList.get(searchDatabaseSizeAfter - 1);

                assertMetadataAllPropertiesEquals(testMetadataSearch, updatedMetadata);
            });
    }

    @Test
    @Transactional
    void putNonExistingMetadata() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        metadata.setId(longCount.incrementAndGet());

        // Create the Metadata
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMetadataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, metadataDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metadataDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Metadata in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchMetadata() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        metadata.setId(longCount.incrementAndGet());

        // Create the Metadata
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetadataMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(metadataDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Metadata in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamMetadata() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        metadata.setId(longCount.incrementAndGet());

        // Create the Metadata
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetadataMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(metadataDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Metadata in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdateMetadataWithPatch() throws Exception {
        // Initialize the database
        insertedMetadata = metadataRepository.saveAndFlush(metadata);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metadata using partial update
        Metadata partialUpdatedMetadata = new Metadata();
        partialUpdatedMetadata.setId(metadata.getId());

        partialUpdatedMetadata.createdAt(UPDATED_CREATED_AT).updatedAt(UPDATED_UPDATED_AT);

        restMetadataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMetadata.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMetadata))
            )
            .andExpect(status().isOk());

        // Validate the Metadata in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetadataUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedMetadata, metadata), getPersistedMetadata(metadata));
    }

    @Test
    @Transactional
    void fullUpdateMetadataWithPatch() throws Exception {
        // Initialize the database
        insertedMetadata = metadataRepository.saveAndFlush(metadata);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the metadata using partial update
        Metadata partialUpdatedMetadata = new Metadata();
        partialUpdatedMetadata.setId(metadata.getId());

        partialUpdatedMetadata
            .fileId(UPDATED_FILE_ID)
            .key(UPDATED_KEY)
            .value(UPDATED_VALUE)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restMetadataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedMetadata.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedMetadata))
            )
            .andExpect(status().isOk());

        // Validate the Metadata in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertMetadataUpdatableFieldsEquals(partialUpdatedMetadata, getPersistedMetadata(partialUpdatedMetadata));
    }

    @Test
    @Transactional
    void patchNonExistingMetadata() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        metadata.setId(longCount.incrementAndGet());

        // Create the Metadata
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restMetadataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, metadataDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(metadataDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Metadata in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchMetadata() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        metadata.setId(longCount.incrementAndGet());

        // Create the Metadata
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetadataMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(metadataDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Metadata in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamMetadata() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        metadata.setId(longCount.incrementAndGet());

        // Create the Metadata
        MetadataDTO metadataDTO = metadataMapper.toDto(metadata);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restMetadataMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(metadataDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Metadata in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deleteMetadata() throws Exception {
        // Initialize the database
        insertedMetadata = metadataRepository.saveAndFlush(metadata);
        metadataRepository.save(metadata);
        metadataSearchRepository.save(metadata);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the metadata
        restMetadataMockMvc
            .perform(delete(ENTITY_API_URL_ID, metadata.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(metadataSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchMetadata() throws Exception {
        // Initialize the database
        insertedMetadata = metadataRepository.saveAndFlush(metadata);
        metadataSearchRepository.save(metadata);

        // Search the metadata
        restMetadataMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + metadata.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(metadata.getId().intValue())))
            .andExpect(jsonPath("$.[*].fileId").value(hasItem(DEFAULT_FILE_ID)))
            .andExpect(jsonPath("$.[*].key").value(hasItem(DEFAULT_KEY)))
            .andExpect(jsonPath("$.[*].value").value(hasItem(DEFAULT_VALUE)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))));
    }

    protected long getRepositoryCount() {
        return metadataRepository.count();
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

    protected Metadata getPersistedMetadata(Metadata metadata) {
        return metadataRepository.findById(metadata.getId()).orElseThrow();
    }

    protected void assertPersistedMetadataToMatchAllProperties(Metadata expectedMetadata) {
        assertMetadataAllPropertiesEquals(expectedMetadata, getPersistedMetadata(expectedMetadata));
    }

    protected void assertPersistedMetadataToMatchUpdatableProperties(Metadata expectedMetadata) {
        assertMetadataAllUpdatablePropertiesEquals(expectedMetadata, getPersistedMetadata(expectedMetadata));
    }
}
