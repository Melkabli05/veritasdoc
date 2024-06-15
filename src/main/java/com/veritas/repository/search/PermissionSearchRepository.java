package com.veritas.repository.search;

import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import com.veritas.domain.Permission;
import com.veritas.repository.PermissionRepository;
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
 * Spring Data Elasticsearch repository for the {@link Permission} entity.
 */
public interface PermissionSearchRepository extends ElasticsearchRepository<Permission, Long>, PermissionSearchRepositoryInternal {}

interface PermissionSearchRepositoryInternal {
    Page<Permission> search(String query, Pageable pageable);

    Page<Permission> search(Query query);

    @Async
    void index(Permission entity);

    @Async
    void deleteFromIndexById(Long id);
}

class PermissionSearchRepositoryInternalImpl implements PermissionSearchRepositoryInternal {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final PermissionRepository repository;

    PermissionSearchRepositoryInternalImpl(ElasticsearchTemplate elasticsearchTemplate, PermissionRepository repository) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.repository = repository;
    }

    @Override
    public Page<Permission> search(String query, Pageable pageable) {
        NativeQuery nativeQuery = new NativeQuery(QueryStringQuery.of(qs -> qs.query(query))._toQuery());
        return search(nativeQuery.setPageable(pageable));
    }

    @Override
    public Page<Permission> search(Query query) {
        SearchHits<Permission> searchHits = elasticsearchTemplate.search(query, Permission.class);
        List<Permission> hits = searchHits.map(SearchHit::getContent).stream().toList();
        return new PageImpl<>(hits, query.getPageable(), searchHits.getTotalHits());
    }

    @Override
    public void index(Permission entity) {
        repository.findById(entity.getId()).ifPresent(elasticsearchTemplate::save);
    }

    @Override
    public void deleteFromIndexById(Long id) {
        elasticsearchTemplate.delete(String.valueOf(id), Permission.class);
    }
}
