package com.veritas.repository;

import com.veritas.domain.FileVersion;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the FileVersion entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Long> {}
