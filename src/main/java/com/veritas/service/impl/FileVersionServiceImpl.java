package com.veritas.service.impl;

import com.veritas.domain.FileVersion;
import com.veritas.repository.FileVersionRepository;
import com.veritas.repository.search.FileVersionSearchRepository;
import com.veritas.service.FileVersionService;
import com.veritas.service.dto.FileVersionDTO;
import com.veritas.service.mapper.FileVersionMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.veritas.domain.FileVersion}.
 */
@Service
@Transactional
public class FileVersionServiceImpl implements FileVersionService {

    private final Logger log = LoggerFactory.getLogger(FileVersionServiceImpl.class);

    private final FileVersionRepository fileVersionRepository;

    private final FileVersionMapper fileVersionMapper;

    private final FileVersionSearchRepository fileVersionSearchRepository;

    public FileVersionServiceImpl(
        FileVersionRepository fileVersionRepository,
        FileVersionMapper fileVersionMapper,
        FileVersionSearchRepository fileVersionSearchRepository
    ) {
        this.fileVersionRepository = fileVersionRepository;
        this.fileVersionMapper = fileVersionMapper;
        this.fileVersionSearchRepository = fileVersionSearchRepository;
    }

    @Override
    public FileVersionDTO save(FileVersionDTO fileVersionDTO) {
        log.debug("Request to save FileVersion : {}", fileVersionDTO);
        FileVersion fileVersion = fileVersionMapper.toEntity(fileVersionDTO);
        fileVersion = fileVersionRepository.save(fileVersion);
        fileVersionSearchRepository.index(fileVersion);
        return fileVersionMapper.toDto(fileVersion);
    }

    @Override
    public FileVersionDTO update(FileVersionDTO fileVersionDTO) {
        log.debug("Request to update FileVersion : {}", fileVersionDTO);
        FileVersion fileVersion = fileVersionMapper.toEntity(fileVersionDTO);
        fileVersion = fileVersionRepository.save(fileVersion);
        fileVersionSearchRepository.index(fileVersion);
        return fileVersionMapper.toDto(fileVersion);
    }

    @Override
    public Optional<FileVersionDTO> partialUpdate(FileVersionDTO fileVersionDTO) {
        log.debug("Request to partially update FileVersion : {}", fileVersionDTO);

        return fileVersionRepository
            .findById(fileVersionDTO.getId())
            .map(existingFileVersion -> {
                fileVersionMapper.partialUpdate(existingFileVersion, fileVersionDTO);

                return existingFileVersion;
            })
            .map(fileVersionRepository::save)
            .map(savedFileVersion -> {
                fileVersionSearchRepository.index(savedFileVersion);
                return savedFileVersion;
            })
            .map(fileVersionMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileVersionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all FileVersions");
        return fileVersionRepository.findAll(pageable).map(fileVersionMapper::toDto);
    }

    /**
     *  Get all the fileVersions where File is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<FileVersionDTO> findAllWhereFileIsNull() {
        log.debug("Request to get all fileVersions where File is null");
        return StreamSupport.stream(fileVersionRepository.findAll().spliterator(), false)
            .filter(fileVersion -> fileVersion.getFile() == null)
            .map(fileVersionMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FileVersionDTO> findOne(Long id) {
        log.debug("Request to get FileVersion : {}", id);
        return fileVersionRepository.findById(id).map(fileVersionMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete FileVersion : {}", id);
        fileVersionRepository.deleteById(id);
        fileVersionSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileVersionDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of FileVersions for query {}", query);
        return fileVersionSearchRepository.search(query, pageable).map(fileVersionMapper::toDto);
    }
}
