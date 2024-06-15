package com.veritas.service.impl;

import com.veritas.domain.Permission;
import com.veritas.repository.PermissionRepository;
import com.veritas.repository.search.PermissionSearchRepository;
import com.veritas.service.PermissionService;
import com.veritas.service.dto.PermissionDTO;
import com.veritas.service.mapper.PermissionMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.veritas.domain.Permission}.
 */
@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final Logger log = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final PermissionRepository permissionRepository;

    private final PermissionMapper permissionMapper;

    private final PermissionSearchRepository permissionSearchRepository;

    public PermissionServiceImpl(
        PermissionRepository permissionRepository,
        PermissionMapper permissionMapper,
        PermissionSearchRepository permissionSearchRepository
    ) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.permissionSearchRepository = permissionSearchRepository;
    }

    @Override
    public PermissionDTO save(PermissionDTO permissionDTO) {
        log.debug("Request to save Permission : {}", permissionDTO);
        Permission permission = permissionMapper.toEntity(permissionDTO);
        permission = permissionRepository.save(permission);
        permissionSearchRepository.index(permission);
        return permissionMapper.toDto(permission);
    }

    @Override
    public PermissionDTO update(PermissionDTO permissionDTO) {
        log.debug("Request to update Permission : {}", permissionDTO);
        Permission permission = permissionMapper.toEntity(permissionDTO);
        permission = permissionRepository.save(permission);
        permissionSearchRepository.index(permission);
        return permissionMapper.toDto(permission);
    }

    @Override
    public Optional<PermissionDTO> partialUpdate(PermissionDTO permissionDTO) {
        log.debug("Request to partially update Permission : {}", permissionDTO);

        return permissionRepository
            .findById(permissionDTO.getId())
            .map(existingPermission -> {
                permissionMapper.partialUpdate(existingPermission, permissionDTO);

                return existingPermission;
            })
            .map(permissionRepository::save)
            .map(savedPermission -> {
                permissionSearchRepository.index(savedPermission);
                return savedPermission;
            })
            .map(permissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Permissions");
        return permissionRepository.findAll(pageable).map(permissionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PermissionDTO> findOne(Long id) {
        log.debug("Request to get Permission : {}", id);
        return permissionRepository.findById(id).map(permissionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Permission : {}", id);
        permissionRepository.deleteById(id);
        permissionSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Permissions for query {}", query);
        return permissionSearchRepository.search(query, pageable).map(permissionMapper::toDto);
    }
}
