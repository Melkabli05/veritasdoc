package com.veritas.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.veritas.domain.FileVersion;
import com.veritas.repository.FileVersionRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

/**
 * Spring Data Elasticsearch repository for the {@link FileVersion} entity.
 */
public interface FileVersionSearchRepository extends ElasticsearchRepository<FileVersion, Long>, FileVersionSearchRepositoryInternal {}

interface FileVersionSearchRepositoryInternal {
    Page<FileVersion> search(String query, Pageable pageable);

    Page<FileVersion> search(Query query);

    @Async
    void index(FileVersion entity);

    @Async
    void deleteFromIndexById(Long id);
}

class FileVersionSearchRepositoryInternalImpl implements FileVersionSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final FileVersionRepository repository;

    FileVersionSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, FileVersionRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<FileVersion> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<FileVersion> search(Query query) {
        SearchHits<FileVersion> searchHits = elasticsearchTemplate.search(query, FileVersion.class);
        List<FileVersion> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(FileVersion entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), FileVersion.class);
    }
}
