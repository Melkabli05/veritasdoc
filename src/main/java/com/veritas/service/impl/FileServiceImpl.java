package com.veritas.service.impl;

import com.veritas.domain.File;
import com.veritas.repository.FileRepository;
import com.veritas.repository.search.FileSearchRepository;
import com.veritas.security.SecurityUtils;
import com.veritas.service.FileService;
import com.veritas.service.StorageService;
import com.veritas.service.dto.*;
import com.veritas.service.mapper.FileMapper;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import io.minio.StatObjectResponse;
import jakarta.validation.constraints.NotNull;
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

    private final StorageService storageService;

    public FileServiceImpl(FileRepository fileRepository, FileMapper fileMapper, FileSearchRepository fileSearchRepository, StorageService storageService) {
        this.fileRepository = fileRepository;
        this.fileMapper = fileMapper;
        this.fileSearchRepository = fileSearchRepository;
        this.storageService = storageService;
    }

    @Override
    public Set<FileDTO> save(FileUploadRequestDTO request) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<StatObjectResponse>> futures = new ArrayList<>();
            List<FileDTO> savedFileDTOs = new ArrayList<>();

            request.files().forEach(file -> {
                CompletableFuture<StatObjectResponse> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        return storageService.uploadFile(file, request.bucketName(), request.folder());
                    } catch (Exception e) {
                        log.error("Error uploading file: {}", e.getMessage());
                        return null;
                    }
                }, executor);
                futures.add(future);
            });

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            futures.forEach(future -> {
                try {
                    StatObjectResponse statObjectResponse = future.get();
                    if (statObjectResponse != null) {
                        savedFileDTOs.add(buildFileDTO(statObjectResponse));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error getting file upload response: {}", e.getMessage());
                }
            });

            List<File> savedFiles = fileRepository.saveAll(savedFileDTOs.stream().map(fileMapper::toEntity).toList());
            savedFiles.forEach(fileSearchRepository::index);

            return savedFiles.stream().map(fileMapper::toDto).collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error in file saving process: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    private FileDTO buildFileDTO(StatObjectResponse statObjectResponse) {
        var fileDTO = new FileDTO();
        fileDTO.setFilename(statObjectResponse.object());
        fileDTO.setBucketName(statObjectResponse.bucket());
        fileDTO.setObjectName(statObjectResponse.object());
        fileDTO.setContentType(statObjectResponse.contentType());
        fileDTO.setFileSize(statObjectResponse.size());
        fileDTO.setUploadedBy(SecurityUtils.getCurrentUserLogin().orElse(null));
        fileDTO.setCreatedAt(statObjectResponse.lastModified());
        return fileDTO;

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
