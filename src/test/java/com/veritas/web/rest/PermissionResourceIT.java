package com.veritas.web.rest;

import static com.veritas.domain.PermissionAsserts.*;
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
import com.veritas.domain.Permission;
import com.veritas.repository.PermissionRepository;
import com.veritas.repository.search.PermissionSearchRepository;
import com.veritas.service.dto.PermissionDTO;
import com.veritas.service.mapper.PermissionMapper;
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
 * Integration tests for the {@link PermissionResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class PermissionResourceIT {

    private static final String DEFAULT_FILE_ID = "AAAAAAAAAA";
    private static final String UPDATED_FILE_ID = "BBBBBBBBBB";

    private static final String DEFAULT_USER_ID = "AAAAAAAAAA";
    private static final String UPDATED_USER_ID = "BBBBBBBBBB";

    private static final String DEFAULT_PERMISSION = "AAAAAAAAAA";
    private static final String UPDATED_PERMISSION = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_UPDATED_AT = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_UPDATED_AT = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final String ENTITY_API_URL = "/api/permissions";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/permissions/_search";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private PermissionSearchRepository permissionSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPermissionMockMvc;

    private Permission permission;

    private Permission insertedPermission;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Permission createEntity(EntityManager em) {
        Permission permission = new Permission()
            .fileId(DEFAULT_FILE_ID)
            .userId(DEFAULT_USER_ID)
            .permission(DEFAULT_PERMISSION)
            .createdAt(DEFAULT_CREATED_AT)
            .updatedAt(DEFAULT_UPDATED_AT);
        return permission;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Permission createUpdatedEntity(EntityManager em) {
        Permission permission = new Permission()
            .fileId(UPDATED_FILE_ID)
            .userId(UPDATED_USER_ID)
            .permission(UPDATED_PERMISSION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        return permission;
    }

    @BeforeEach
    public void initTest() {
        permission = createEntity(em);
    }

    @AfterEach
    public void cleanup() {
        if (insertedPermission != null) {
            permissionRepository.delete(insertedPermission);
            permissionSearchRepository.delete(insertedPermission);
            insertedPermission = null;
        }
    }

    @Test
    @Transactional
    void createPermission() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);
        var returnedPermissionDTO = om.readValue(
            restPermissionMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(permissionDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            PermissionDTO.class
        );

        // Validate the Permission in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedPermission = permissionMapper.toEntity(returnedPermissionDTO);
        assertPermissionUpdatableFieldsEquals(returnedPermission, getPersistedPermission(returnedPermission));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore + 1);
            });

        insertedPermission = returnedPermission;
    }

    @Test
    @Transactional
    void createPermissionWithExistingId() throws Exception {
        // Create the Permission with an existing ID
        permission.setId(1L);
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        long databaseSizeBeforeCreate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());

        // An entity with an existing ID cannot be created, so this API call must fail
        restPermissionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(permissionDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Permission in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkFileIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        // set the field null
        permission.setFileId(null);

        // Create the Permission, which fails.
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        restPermissionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(permissionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkUserIdIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        // set the field null
        permission.setUserId(null);

        // Create the Permission, which fails.
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        restPermissionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(permissionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void checkPermissionIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        // set the field null
        permission.setPermission(null);

        // Create the Permission, which fails.
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        restPermissionMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(permissionDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);

        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void getAllPermissions() throws Exception {
        // Initialize the database
        insertedPermission = permissionRepository.saveAndFlush(permission);

        // Get all the permissionList
        restPermissionMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(permission.getId().intValue())))
            .andExpect(jsonPath("$.[*].fileId").value(hasItem(DEFAULT_FILE_ID)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].permission").value(hasItem(DEFAULT_PERMISSION)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))));
    }

    @Test
    @Transactional
    void getPermission() throws Exception {
        // Initialize the database
        insertedPermission = permissionRepository.saveAndFlush(permission);

        // Get the permission
        restPermissionMockMvc
            .perform(get(ENTITY_API_URL_ID, permission.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(permission.getId().intValue()))
            .andExpect(jsonPath("$.fileId").value(DEFAULT_FILE_ID))
            .andExpect(jsonPath("$.userId").value(DEFAULT_USER_ID))
            .andExpect(jsonPath("$.permission").value(DEFAULT_PERMISSION))
            .andExpect(jsonPath("$.createdAt").value(sameInstant(DEFAULT_CREATED_AT)))
            .andExpect(jsonPath("$.updatedAt").value(sameInstant(DEFAULT_UPDATED_AT)));
    }

    @Test
    @Transactional
    void getNonExistingPermission() throws Exception {
        // Get the permission
        restPermissionMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingPermission() throws Exception {
        // Initialize the database
        insertedPermission = permissionRepository.saveAndFlush(permission);

        long databaseSizeBeforeUpdate = getRepositoryCount();
        permissionSearchRepository.save(permission);
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());

        // Update the permission
        Permission updatedPermission = permissionRepository.findById(permission.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedPermission are not directly saved in db
        em.detach(updatedPermission);
        updatedPermission
            .fileId(UPDATED_FILE_ID)
            .userId(UPDATED_USER_ID)
            .permission(UPDATED_PERMISSION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);
        PermissionDTO permissionDTO = permissionMapper.toDto(updatedPermission);

        restPermissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, permissionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(permissionDTO))
            )
            .andExpect(status().isOk());

        // Validate the Permission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedPermissionToMatchAllProperties(updatedPermission);

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
                assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
                List<Permission> permissionSearchList = Streamable.of(permissionSearchRepository.findAll()).toList();
                Permission testPermissionSearch = permissionSearchList.get(searchDatabaseSizeAfter - 1);

                assertPermissionAllPropertiesEquals(testPermissionSearch, updatedPermission);
            });
    }

    @Test
    @Transactional
    void putNonExistingPermission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        permission.setId(longCount.incrementAndGet());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPermissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, permissionDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(permissionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Permission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithIdMismatchPermission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        permission.setId(longCount.incrementAndGet());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPermissionMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(permissionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Permission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPermission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        permission.setId(longCount.incrementAndGet());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPermissionMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(permissionDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Permission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void partialUpdatePermissionWithPatch() throws Exception {
        // Initialize the database
        insertedPermission = permissionRepository.saveAndFlush(permission);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the permission using partial update
        Permission partialUpdatedPermission = new Permission();
        partialUpdatedPermission.setId(permission.getId());

        partialUpdatedPermission
            .fileId(UPDATED_FILE_ID)
            .userId(UPDATED_USER_ID)
            .permission(UPDATED_PERMISSION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restPermissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPermission.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPermission))
            )
            .andExpect(status().isOk());

        // Validate the Permission in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPermissionUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedPermission, permission),
            getPersistedPermission(permission)
        );
    }

    @Test
    @Transactional
    void fullUpdatePermissionWithPatch() throws Exception {
        // Initialize the database
        insertedPermission = permissionRepository.saveAndFlush(permission);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the permission using partial update
        Permission partialUpdatedPermission = new Permission();
        partialUpdatedPermission.setId(permission.getId());

        partialUpdatedPermission
            .fileId(UPDATED_FILE_ID)
            .userId(UPDATED_USER_ID)
            .permission(UPDATED_PERMISSION)
            .createdAt(UPDATED_CREATED_AT)
            .updatedAt(UPDATED_UPDATED_AT);

        restPermissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPermission.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedPermission))
            )
            .andExpect(status().isOk());

        // Validate the Permission in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPermissionUpdatableFieldsEquals(partialUpdatedPermission, getPersistedPermission(partialUpdatedPermission));
    }

    @Test
    @Transactional
    void patchNonExistingPermission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        permission.setId(longCount.incrementAndGet());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPermissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, permissionDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(permissionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Permission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPermission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        permission.setId(longCount.incrementAndGet());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPermissionMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(permissionDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Permission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPermission() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        permission.setId(longCount.incrementAndGet());

        // Create the Permission
        PermissionDTO permissionDTO = permissionMapper.toDto(permission);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPermissionMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(permissionDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Permission in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore);
    }

    @Test
    @Transactional
    void deletePermission() throws Exception {
        // Initialize the database
        insertedPermission = permissionRepository.saveAndFlush(permission);
        permissionRepository.save(permission);
        permissionSearchRepository.save(permission);

        long databaseSizeBeforeDelete = getRepositoryCount();
        int searchDatabaseSizeBefore = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeBefore).isEqualTo(databaseSizeBeforeDelete);

        // Delete the permission
        restPermissionMockMvc
            .perform(delete(ENTITY_API_URL_ID, permission.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
        int searchDatabaseSizeAfter = IterableUtil.sizeOf(permissionSearchRepository.findAll());
        assertThat(searchDatabaseSizeAfter).isEqualTo(searchDatabaseSizeBefore - 1);
    }

    @Test
    @Transactional
    void searchPermission() throws Exception {
        // Initialize the database
        insertedPermission = permissionRepository.saveAndFlush(permission);
        permissionSearchRepository.save(permission);

        // Search the permission
        restPermissionMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + permission.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(permission.getId().intValue())))
            .andExpect(jsonPath("$.[*].fileId").value(hasItem(DEFAULT_FILE_ID)))
            .andExpect(jsonPath("$.[*].userId").value(hasItem(DEFAULT_USER_ID)))
            .andExpect(jsonPath("$.[*].permission").value(hasItem(DEFAULT_PERMISSION)))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(sameInstant(DEFAULT_CREATED_AT))))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(sameInstant(DEFAULT_UPDATED_AT))));
    }

    protected long getRepositoryCount() {
        return permissionRepository.count();
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

    protected Permission getPersistedPermission(Permission permission) {
        return permissionRepository.findById(permission.getId()).orElseThrow();
    }

    protected void assertPersistedPermissionToMatchAllProperties(Permission expectedPermission) {
        assertPermissionAllPropertiesEquals(expectedPermission, getPersistedPermission(expectedPermission));
    }

    protected void assertPersistedPermissionToMatchUpdatableProperties(Permission expectedPermission) {
        assertPermissionAllUpdatablePropertiesEquals(expectedPermission, getPersistedPermission(expectedPermission));
    }
}
