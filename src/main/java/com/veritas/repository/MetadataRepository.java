package com.veritas.repository;

import com.veritas.domain.Metadata;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Metadata entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MetadataRepository extends JpaRepository<Metadata, Long> {}
