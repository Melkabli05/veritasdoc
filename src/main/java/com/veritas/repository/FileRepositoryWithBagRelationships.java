package com.veritas.repository;

import com.veritas.domain.File;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface FileRepositoryWithBagRelationships {
    Optional<File> fetchBagRelationships(Optional<File> file);

    List<File> fetchBagRelationships(List<File> files);

    Page<File> fetchBagRelationships(Page<File> files);
}
