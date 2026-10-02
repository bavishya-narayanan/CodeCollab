package com.codecollab.snippet.repository;

import com.codecollab.snippet.entity.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SnippetRepository extends JpaRepository<Snippet, Long> {

    // Find non-deleted by id
    Optional<Snippet> findByIdAndDeletedFalse(Long id);
    
    // Check if exists
    boolean existsByIdAndDeletedFalse(Long id);

    // List all non-deleted, paginated
    Page<Snippet> findAllByDeletedFalse(Pageable pageable);

    // List by owner, non-deleted, paginated
    Page<Snippet> findAllByOwnerIdAndDeletedFalse(Long ownerId, Pageable pageable);

    // Count for user
    long countByOwnerIdAndDeletedFalse(Long ownerId);

    // Atomic view increment
    @Modifying
    @Query("UPDATE Snippet s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // For Phase 3 (search) — leave a placeholder comment
    // For Phase 4 (AST) — leave a placeholder comment
    // For Phase 5 (votes) — leave a placeholder comment
}
