package com.veritas.service.impl;

import com.veritas.domain.Metadata;
import com.veritas.repository.MetadataRepository;
import com.veritas.repository.search.MetadataSearchRepository;
import com.veritas.service.MetadataService;
import com.veritas.service.dto.MetadataDTO;
import com.veritas.service.mapper.MetadataMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.veritas.domain.Metadata}.
 */
@Service
@Transactional
public class MetadataServiceImpl implements MetadataService {

    private final Logger log = LoggerFactory.getLogger(MetadataServiceImpl.class);

    private final MetadataRepository metadataRepository;

    private final MetadataMapper metadataMapper;

    private final MetadataSearchRepository metadataSearchRepository;

    public MetadataServiceImpl(
        MetadataRepository metadataRepository,
        MetadataMapper metadataMapper,
        MetadataSearchRepository metadataSearchRepository
    ) {
        this.metadataRepository = metadataRepository;
        this.metadataMapper = metadataMapper;
        this.metadataSearchRepository = metadataSearchRepository;
    }

    @Override
    public MetadataDTO save(MetadataDTO metadataDTO) {
        log.debug("Request to save Metadata : {}", metadataDTO);
        Metadata metadata = metadataMapper.toEntity(metadataDTO);
        metadata = metadataRepository.save(metadata);
        metadataSearchRepository.index(metadata);
        return metadataMapper.toDto(metadata);
    }

    @Override
    public MetadataDTO update(MetadataDTO metadataDTO) {
        log.debug("Request to update Metadata : {}", metadataDTO);
        Metadata metadata = metadataMapper.toEntity(metadataDTO);
        metadata = metadataRepository.save(metadata);
        metadataSearchRepository.index(metadata);
        return metadataMapper.toDto(metadata);
    }

    @Override
    public Optional<MetadataDTO> partialUpdate(MetadataDTO metadataDTO) {
        log.debug("Request to partially update Metadata : {}", metadataDTO);

        return metadataRepository
            .findById(metadataDTO.getId())
            .map(existingMetadata -> {
                metadataMapper.partialUpdate(existingMetadata, metadataDTO);

                return existingMetadata;
            })
            .map(metadataRepository::save)
            .map(savedMetadata -> {
                metadataSearchRepository.index(savedMetadata);
                return savedMetadata;
            })
            .map(metadataMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MetadataDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Metadata");
        return metadataRepository.findAll(pageable).map(metadataMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MetadataDTO> findOne(Long id) {
        log.debug("Request to get Metadata : {}", id);
        return metadataRepository.findById(id).map(metadataMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Metadata : {}", id);
        metadataRepository.deleteById(id);
        metadataSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MetadataDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Metadata for query {}", query);
        return metadataSearchRepository.search(query, pageable).map(metadataMapper::toDto);
    }
}
