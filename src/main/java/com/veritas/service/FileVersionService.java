package com.veritas.service;

import com.veritas.service.dto.FileVersionDTO;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.veritas.domain.FileVersion}.
 */
public interface FileVersionService {
    /**
     * Save a fileVersion.
     *
     * @param fileVersionDTO the entity to save.
     * @return the persisted entity.
     */
    FileVersionDTO save(FileVersionDTO fileVersionDTO);

    /**
     * Updates a fileVersion.
     *
     * @param fileVersionDTO the entity to update.
     * @return the persisted entity.
     */
    FileVersionDTO update(FileVersionDTO fileVersionDTO);

    /**
     * Partially updates a fileVersion.
     *
     * @param fileVersionDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FileVersionDTO> partialUpdate(FileVersionDTO fileVersionDTO);

    /**
     * Get all the fileVersions.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FileVersionDTO> findAll(Pageable pageable);

    /**
     * Get all the FileVersionDTO where File is {@code null}.
     *
     * @return the {@link List} of entities.
     */
    List<FileVersionDTO> findAllWhereFileIsNull();

    /**
     * Get the "id" fileVersion.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FileVersionDTO> findOne(Long id);

    /**
     * Delete the "id" fileVersion.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the fileVersion corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FileVersionDTO> search(String query, Pageable pageable);
}
