package com.veritas.service;

import com.veritas.service.dto.FileDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.veritas.service.dto.FileUploadRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.veritas.domain.File}.
 */
public interface FileService {
    /**
     * Save a file or files.
     *
     * @param requestDTO the entity to save.
     * @return the persisted entity(s).
     */
    Set<FileDTO> save(FileUploadRequestDTO requestDTO);

    /**
     * Updates a file.
     *
     * @param fileDTO the entity to update.
     * @return the persisted entity.
     */
    FileDTO update(FileDTO fileDTO);

    /**
     * Partially updates a file.
     *
     * @param fileDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FileDTO> partialUpdate(FileDTO fileDTO);

    /**
     * Get all the files.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FileDTO> findAll(Pageable pageable);

    /**
     * Get all the files with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FileDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" file.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FileDTO> findOne(Long id);

    /**
     * Delete the "id" file.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the file corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FileDTO> search(String query, Pageable pageable);
}
