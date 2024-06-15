package com.veritas.service;

import com.veritas.service.dto.MetadataDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.veritas.domain.Metadata}.
 */
public interface MetadataService {
    /**
     * Save a metadata.
     *
     * @param metadataDTO the entity to save.
     * @return the persisted entity.
     */
    MetadataDTO save(MetadataDTO metadataDTO);

    /**
     * Updates a metadata.
     *
     * @param metadataDTO the entity to update.
     * @return the persisted entity.
     */
    MetadataDTO update(MetadataDTO metadataDTO);

    /**
     * Partially updates a metadata.
     *
     * @param metadataDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<MetadataDTO> partialUpdate(MetadataDTO metadataDTO);

    /**
     * Get all the metadata.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MetadataDTO> findAll(Pageable pageable);

    /**
     * Get the "id" metadata.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<MetadataDTO> findOne(Long id);

    /**
     * Delete the "id" metadata.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Search for the metadata corresponding to the query.
     *
     * @param query the query of the search.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<MetadataDTO> search(String query, Pageable pageable);
}
