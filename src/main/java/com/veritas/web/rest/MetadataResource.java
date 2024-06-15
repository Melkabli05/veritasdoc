package com.veritas.web.rest;

import com.veritas.repository.MetadataRepository;
import com.veritas.service.MetadataService;
import com.veritas.service.dto.MetadataDTO;
import com.veritas.web.rest.errors.BadRequestAlertException;
import com.veritas.web.rest.errors.ElasticsearchExceptionMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.veritas.domain.Metadata}.
 */
@RestController
@RequestMapping("/api/metadata")
public class MetadataResource {

    private final Logger log = LoggerFactory.getLogger(MetadataResource.class);

    private static final String ENTITY_NAME = "metadata";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MetadataService metadataService;

    private final MetadataRepository metadataRepository;

    public MetadataResource(MetadataService metadataService, MetadataRepository metadataRepository) {
        this.metadataService = metadataService;
        this.metadataRepository = metadataRepository;
    }

    /**
     * {@code POST  /metadata} : Create a new metadata.
     *
     * @param metadataDTO the metadataDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new metadataDTO, or with status {@code 400 (Bad Request)} if the metadata has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<MetadataDTO> createMetadata(@Valid @RequestBody MetadataDTO metadataDTO) throws URISyntaxException {
        log.debug("REST request to save Metadata : {}", metadataDTO);
        if (metadataDTO.getId() != null) {
            throw new BadRequestAlertException("A new metadata cannot already have an ID", ENTITY_NAME, "idexists");
        }
        metadataDTO = metadataService.save(metadataDTO);
        return ResponseEntity.created(new URI("/api/metadata/" + metadataDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, metadataDTO.getId().toString()))
            .body(metadataDTO);
    }

    /**
     * {@code PUT  /metadata/:id} : Updates an existing metadata.
     *
     * @param id the id of the metadataDTO to save.
     * @param metadataDTO the metadataDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated metadataDTO,
     * or with status {@code 400 (Bad Request)} if the metadataDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the metadataDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MetadataDTO> updateMetadata(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody MetadataDTO metadataDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Metadata : {}, {}", id, metadataDTO);
        if (metadataDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, metadataDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!metadataRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        metadataDTO = metadataService.update(metadataDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, metadataDTO.getId().toString()))
            .body(metadataDTO);
    }

    /**
     * {@code PATCH  /metadata/:id} : Partial updates given fields of an existing metadata, field will ignore if it is null
     *
     * @param id the id of the metadataDTO to save.
     * @param metadataDTO the metadataDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated metadataDTO,
     * or with status {@code 400 (Bad Request)} if the metadataDTO is not valid,
     * or with status {@code 404 (Not Found)} if the metadataDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the metadataDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MetadataDTO> partialUpdateMetadata(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody MetadataDTO metadataDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Metadata partially : {}, {}", id, metadataDTO);
        if (metadataDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, metadataDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!metadataRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MetadataDTO> result = metadataService.partialUpdate(metadataDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, metadataDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /metadata} : get all the metadata.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of metadata in body.
     */
    @GetMapping("")
    public ResponseEntity<List<MetadataDTO>> getAllMetadata(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Metadata");
        Page<MetadataDTO> page = metadataService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /metadata/:id} : get the "id" metadata.
     *
     * @param id the id of the metadataDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the metadataDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MetadataDTO> getMetadata(@PathVariable("id") Long id) {
        log.debug("REST request to get Metadata : {}", id);
        Optional<MetadataDTO> metadataDTO = metadataService.findOne(id);
        return ResponseUtil.wrapOrNotFound(metadataDTO);
    }

    /**
     * {@code DELETE  /metadata/:id} : delete the "id" metadata.
     *
     * @param id the id of the metadataDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetadata(@PathVariable("id") Long id) {
        log.debug("REST request to delete Metadata : {}", id);
        metadataService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /metadata/_search?query=:query} : search for the metadata corresponding
     * to the query.
     *
     * @param query the query of the metadata search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<MetadataDTO>> searchMetadata(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of Metadata for query {}", query);
        try {
            Page<MetadataDTO> page = metadataService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
