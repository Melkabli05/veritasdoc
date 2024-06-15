package com.veritas.web.rest;

import com.veritas.repository.FolderRepository;
import com.veritas.service.FolderService;
import com.veritas.service.dto.FolderDTO;
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
 * REST controller for managing {@link com.veritas.domain.Folder}.
 */
@RestController
@RequestMapping("/api/folders")
public class FolderResource {

    private final Logger log = LoggerFactory.getLogger(FolderResource.class);

    private static final String ENTITY_NAME = "folder";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FolderService folderService;

    private final FolderRepository folderRepository;

    public FolderResource(FolderService folderService, FolderRepository folderRepository) {
        this.folderService = folderService;
        this.folderRepository = folderRepository;
    }

    /**
     * {@code POST  /folders} : Create a new folder.
     *
     * @param folderDTO the folderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new folderDTO, or with status {@code 400 (Bad Request)} if the folder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<FolderDTO> createFolder(@Valid @RequestBody FolderDTO folderDTO) throws URISyntaxException {
        log.debug("REST request to save Folder : {}", folderDTO);
        if (folderDTO.getId() != null) {
            throw new BadRequestAlertException("A new folder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        folderDTO = folderService.save(folderDTO);
        return ResponseEntity.created(new URI("/api/folders/" + folderDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, folderDTO.getId().toString()))
            .body(folderDTO);
    }

    /**
     * {@code PUT  /folders/:id} : Updates an existing folder.
     *
     * @param id the id of the folderDTO to save.
     * @param folderDTO the folderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated folderDTO,
     * or with status {@code 400 (Bad Request)} if the folderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the folderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FolderDTO> updateFolder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody FolderDTO folderDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Folder : {}, {}", id, folderDTO);
        if (folderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, folderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!folderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        folderDTO = folderService.update(folderDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, folderDTO.getId().toString()))
            .body(folderDTO);
    }

    /**
     * {@code PATCH  /folders/:id} : Partial updates given fields of an existing folder, field will ignore if it is null
     *
     * @param id the id of the folderDTO to save.
     * @param folderDTO the folderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated folderDTO,
     * or with status {@code 400 (Bad Request)} if the folderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the folderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the folderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<FolderDTO> partialUpdateFolder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody FolderDTO folderDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Folder partially : {}, {}", id, folderDTO);
        if (folderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, folderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!folderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<FolderDTO> result = folderService.partialUpdate(folderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, folderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /folders} : get all the folders.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of folders in body.
     */
    @GetMapping("")
    public ResponseEntity<List<FolderDTO>> getAllFolders(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get a page of Folders");
        Page<FolderDTO> page = folderService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /folders/:id} : get the "id" folder.
     *
     * @param id the id of the folderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the folderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<FolderDTO> getFolder(@PathVariable("id") Long id) {
        log.debug("REST request to get Folder : {}", id);
        Optional<FolderDTO> folderDTO = folderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(folderDTO);
    }

    /**
     * {@code DELETE  /folders/:id} : delete the "id" folder.
     *
     * @param id the id of the folderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable("id") Long id) {
        log.debug("REST request to delete Folder : {}", id);
        folderService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /folders/_search?query=:query} : search for the folder corresponding
     * to the query.
     *
     * @param query the query of the folder search.
     * @param pageable the pagination information.
     * @return the result of the search.
     */
    @GetMapping("/_search")
    public ResponseEntity<List<FolderDTO>> searchFolders(
        @RequestParam("query") String query,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to search for a page of Folders for query {}", query);
        try {
            Page<FolderDTO> page = folderService.search(query, pageable);
            HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } catch (RuntimeException e) {
            throw ElasticsearchExceptionMapper.mapException(e);
        }
    }
}
