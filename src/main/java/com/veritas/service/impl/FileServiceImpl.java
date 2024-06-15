package com.veritas.service.impl;

import com.veritas.domain.File;
import com.veritas.repository.FileRepository;
import com.veritas.repository.search.FileSearchRepository;
import com.veritas.service.FileService;
import com.veritas.service.dto.FileDTO;
import com.veritas.service.mapper.FileMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.veritas.domain.File}.
 */
@Service
@Transactional
public class FileServiceImpl implements FileService {

    private final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;

    private final FileMapper fileMapper;

    private final FileSearchRepository fileSearchRepository;

    public FileServiceImpl(FileRepository fileRepository, FileMapper fileMapper, FileSearchRepository fileSearchRepository) {
        this.fileRepository = fileRepository;
        this.fileMapper = fileMapper;
        this.fileSearchRepository = fileSearchRepository;
    }

    @Override
    public FileDTO save(FileDTO fileDTO) {
        log.debug("Request to save File : {}", fileDTO);
        File file = fileMapper.toEntity(fileDTO);
        file = fileRepository.save(file);
        fileSearchRepository.index(file);
        return fileMapper.toDto(file);
    }

    @Override
    public FileDTO update(FileDTO fileDTO) {
        log.debug("Request to update File : {}", fileDTO);
        File file = fileMapper.toEntity(fileDTO);
        file = fileRepository.save(file);
        fileSearchRepository.index(file);
        return fileMapper.toDto(file);
    }

    @Override
    public Optional<FileDTO> partialUpdate(FileDTO fileDTO) {
        log.debug("Request to partially update File : {}", fileDTO);

        return fileRepository
            .findById(fileDTO.getId())
            .map(existingFile -> {
                fileMapper.partialUpdate(existingFile, fileDTO);

                return existingFile;
            })
            .map(fileRepository::save)
            .map(savedFile -> {
                fileSearchRepository.index(savedFile);
                return savedFile;
            })
            .map(fileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Files");
        return fileRepository.findAll(pageable).map(fileMapper::toDto);
    }

    public Page<FileDTO> findAllWithEagerRelationships(Pageable pageable) {
        return fileRepository.findAllWithEagerRelationships(pageable).map(fileMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FileDTO> findOne(Long id) {
        log.debug("Request to get File : {}", id);
        return fileRepository.findOneWithEagerRelationships(id).map(fileMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete File : {}", id);
        fileRepository.deleteById(id);
        fileSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Files for query {}", query);
        return fileSearchRepository.search(query, pageable).map(fileMapper::toDto);
    }
}
