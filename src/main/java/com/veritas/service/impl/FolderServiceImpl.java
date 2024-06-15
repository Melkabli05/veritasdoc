package com.veritas.service.impl;

import com.veritas.domain.Folder;
import com.veritas.repository.FolderRepository;
import com.veritas.repository.search.FolderSearchRepository;
import com.veritas.service.FolderService;
import com.veritas.service.dto.FolderDTO;
import com.veritas.service.mapper.FolderMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.veritas.domain.Folder}.
 */
@Service
@Transactional
public class FolderServiceImpl implements FolderService {

    private final Logger log = LoggerFactory.getLogger(FolderServiceImpl.class);

    private final FolderRepository folderRepository;

    private final FolderMapper folderMapper;

    private final FolderSearchRepository folderSearchRepository;

    public FolderServiceImpl(FolderRepository folderRepository, FolderMapper folderMapper, FolderSearchRepository folderSearchRepository) {
        this.folderRepository = folderRepository;
        this.folderMapper = folderMapper;
        this.folderSearchRepository = folderSearchRepository;
    }

    @Override
    public FolderDTO save(FolderDTO folderDTO) {
        log.debug("Request to save Folder : {}", folderDTO);
        Folder folder = folderMapper.toEntity(folderDTO);
        folder = folderRepository.save(folder);
        folderSearchRepository.index(folder);
        return folderMapper.toDto(folder);
    }

    @Override
    public FolderDTO update(FolderDTO folderDTO) {
        log.debug("Request to update Folder : {}", folderDTO);
        Folder folder = folderMapper.toEntity(folderDTO);
        folder = folderRepository.save(folder);
        folderSearchRepository.index(folder);
        return folderMapper.toDto(folder);
    }

    @Override
    public Optional<FolderDTO> partialUpdate(FolderDTO folderDTO) {
        log.debug("Request to partially update Folder : {}", folderDTO);

        return folderRepository
            .findById(folderDTO.getId())
            .map(existingFolder -> {
                folderMapper.partialUpdate(existingFolder, folderDTO);

                return existingFolder;
            })
            .map(folderRepository::save)
            .map(savedFolder -> {
                folderSearchRepository.index(savedFolder);
                return savedFolder;
            })
            .map(folderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FolderDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Folders");
        return folderRepository.findAll(pageable).map(folderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FolderDTO> findOne(Long id) {
        log.debug("Request to get Folder : {}", id);
        return folderRepository.findById(id).map(folderMapper::toDto);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Folder : {}", id);
        folderRepository.deleteById(id);
        folderSearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FolderDTO> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Folders for query {}", query);
        return folderSearchRepository.search(query, pageable).map(folderMapper::toDto);
    }
}
