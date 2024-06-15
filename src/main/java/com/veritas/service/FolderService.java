package com.veritas.service;

import com.veritas.service.dto.FolderDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.veritas.domain.Folder}.
 */
public interface FolderService {
    /**
     * Save a folder.
     *
     * @param folderDTO the entity to save.
     * @return the persisted entity.
     */
    FolderDTO save(FolderDTO folderDTO);

    /**
     * Updates a folder.
     *
     * @param folderDTO the entity to update.
     * @return the persisted entity.
     */
    FolderDTO update(FolderDTO folderDTO);

    /**
     * Partially updates a folder.
     *
     * @param folderDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<FolderDTO> partialUpdate(FolderDTO folderDTO);

    /**
     * Get all the folders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FolderDTO> findAll(Pageable pageable);

    /**
     * Get the "id" folder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<FolderDTO> findOne(Long id);

    /**
     * Delete the "id" folder.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the folder corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<FolderDTO> search(String query, Pageable pageable);
}
