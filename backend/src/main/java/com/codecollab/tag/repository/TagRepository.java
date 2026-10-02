package com.codecollab.tag.repository;

import com.codecollab.tag.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    // For autocomplete — ordered by popularity then alphabetically
    @Query("""
        SELECT t FROM Tag t
        WHERE LOWER(t.name) LIKE LOWER(CONCAT(:prefix, '%'))
        ORDER BY t.usageCount DESC, t.name ASC
        """)
    List<Tag> searchByPrefix(@Param("prefix") String prefix, Pageable pageable);

    // Top tags by usage
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC, t.name ASC")
    Page<Tag> findTopTags(Pageable pageable);

    // Atomic usage count increment
    @Modifying
    @Query("UPDATE Tag t SET t.usageCount = t.usageCount + 1 WHERE t.id = :id")
    void incrementUsage(@Param("id") Long id);

    // Atomic usage count decrement (floor at 0 handled by service)
    @Modifying
    @Query("UPDATE Tag t SET t.usageCount = t.usageCount - 1 WHERE t.id = :id AND t.usageCount > 0")
    void decrementUsage(@Param("id") Long id);
}
