package com.veritas.repository;

import com.veritas.domain.Folder;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Folder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {}
