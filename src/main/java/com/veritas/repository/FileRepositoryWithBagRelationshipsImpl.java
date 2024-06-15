package com.veritas.repository;

import com.veritas.domain.File;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class FileRepositoryWithBagRelationshipsImpl implements FileRepositoryWithBagRelationships {

    private static final String ID_PARAMETER = "id";
    private static final String FILES_PARAMETER = "files";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<File> fetchBagRelationships(Optional<File> file) {
        return file.map(this::fetchTags).map(this::fetchFolders);
    }

    @Override
    public Page<File> fetchBagRelationships(Page<File> files) {
        return new PageImpl<>(fetchBagRelationships(files.getContent()), files.getPageable(), files.getTotalElements());
    }

    @Override
    public List<File> fetchBagRelationships(List<File> files) {
        return Optional.of(files).map(this::fetchTags).map(this::fetchFolders).orElse(Collections.emptyList());
    }

    File fetchTags(File result) {
        return entityManager
            .createQuery("select file from File file left join fetch file.tags where file.id = :id", File.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<File> fetchTags(List<File> files) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, files.size()).forEach(index -> order.put(files.get(index).getId(), index));
        List<File> result = entityManager
            .createQuery("select file from File file left join fetch file.tags where file in :files", File.class)
            .setParameter(FILES_PARAMETER, files)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }

    File fetchFolders(File result) {
        return entityManager
            .createQuery("select file from File file left join fetch file.folders where file.id = :id", File.class)
            .setParameter(ID_PARAMETER, result.getId())
            .getSingleResult();
    }

    List<File> fetchFolders(List<File> files) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, files.size()).forEach(index -> order.put(files.get(index).getId(), index));
        List<File> result = entityManager
            .createQuery("select file from File file left join fetch file.folders where file in :files", File.class)
            .setParameter(FILES_PARAMETER, files)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
