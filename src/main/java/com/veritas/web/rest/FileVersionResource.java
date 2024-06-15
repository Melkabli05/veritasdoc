package com.veritas.web.rest;

import com.veritas.repository.FileVersionRepository;
import com.veritas.service.FileVersionService;
import com.veritas.service.dto.FileVersionDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.veritas.domain.FileVersion}.
 */
@RestController
@RequestMapping("/api/file-versions")
public class FileVersionResource {

    private final Logger log = LoggerFactory.getLogger(FileVersionResource.class);

    private static final String ENTITY_NAME = "fileVersion";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FileVersionService fileVersionService;

    private final FileVersionRepository fileVersionRepository;

    public FileVersionResource(FileVersionService fileVersionService, FileVersionRepository fileVersionRepository) {
        this.fileVersionService = fileVersionService;
        this.fileVersionRepository = fileVersionRepository;
    }

    /**
     * {@code POST  /file-versions} : Create a new fileVersion.
     *
     * @param fileVersionDTO the fileVersionDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new fileVersionDTO, or with status {@code 400 (Bad Request)} if the fileVersion has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FileVersionDTO> createFileVersion(@Valid @RequestBody FileVersionDTO fileVersionDTO) throws URISyntaxException {
        log.debug("REST request to save FileVersion : {}", fileVersionDTO);
        if (fileVersionDTO.getId() != null) {
            throw new BadRequestAlertException("A new fileVersion cannot already have an ID", ENTITY_NAME, "idexists");
        }
        fileVersionDTO = fileVersionService.save(fileVersionDTO);
        return ResponseEntity.created(new URI("/api/file-versions/" + fileVersionDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, fileVersionDTO.getId().toString()))
            .body(fileVersionDTO);
    }

    /**
     * {@code PUT  /file-versions/:id} : Updates an existing fileVersion.
     *
     * @param id the id of the fileVersionDTO to save.
     * @param fileVersionDTO the fileVersionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fileVersionDTO,
     * or with status {@code 400 (Bad Request)} if the fileVersionDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the fileVersionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FileVersionDTO> updateFileVersion(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FileVersionDTO fileVersionDTO
    ) throws URISyntaxException {
        log.debug("REST request to update FileVersion : {}, {}", id, fileVersionDTO);
        if (fileVersionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fileVersionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fileVersionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        fileVersionDTO = fileVersionService.update(fileVersionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fileVersionDTO.getId().toString()))
            .body(fileVersionDTO);
    }

    /**
     * {@code PATCH  /file-versions/:id} : Partial updates given fields of an existing fileVersion, field will ignore if it is null
     *
     * @param id the id of the fileVersionDTO to save.
     * @param fileVersionDTO the fileVersionDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fileVersionDTO,
     * or with status {@code 400 (Bad Request)} if the fileVersionDTO is not valid,
     * or with status {@code 404 (Not Found)} if the fileVersionDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the fileVersionDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FileVersionDTO> partialUpdateFileVersion(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FileVersionDTO fileVersionDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update FileVersion partially : {}, {}", id, fileVersionDTO);
        if (fileVersionDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, fileVersionDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!fileVersionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FileVersionDTO> result = fileVersionService.partialUpdate(fileVersionDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fileVersionDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /file-versions} : get all the fileVersions.
     *
     * @param pageable the pagination information.
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fileVersions in body.
     */
    @GetMapping("")
    public ResponseEntity<List<FileVersionDTO>> getAllFileVersions(
        @org.springdoc.core.annotations.ParameterObject Pageable pageable,
        @RequestParam(name = "filter", required = false) String filter
    ) {
        if ("file-is-null".equals(filter)) {
            log.debug("REST request to get all FileVersions where file is null");
            return new ResponseEntity<>(fileVersionService.findAllWhereFileIsNull(), HttpStatus.OK);
        }
        log.debug("REST request to get a page of FileVersions");
        Page<FileVersionDTO> page = fileVersionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /file-versions/:id} : get the "id" fileVersion.
     *
     * @param id the id of the fileVersionDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileVersionDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FileVersionDTO> getFileVersion(@PathVariable("id") Long id) {
        log.debug("REST request to get FileVersion : {}", id);
        Optional<FileVersionDTO> fileVersionDTO = fileVersionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(fileVersionDTO);
    }

    /**
     * {@code DELETE  /file-versions/:id} : delete the "id" fileVersion.
     *
     * @param id the id of the fileVersionDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFileVersion(@PathVariable("id") Long id) {
        log.debug("REST request to delete FileVersion : {}", id);
        fileVersionService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /file-versions/_search?query=:query} : search for the fileVersion corresponding
     * to the query.
     *
     * @param query the query of the fileVersion search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<FileVersionDTO>> searchFileVersions(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of FileVersions for query {}", query);
        try {
            Page<FileVersionDTO> page = fileVersionService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
