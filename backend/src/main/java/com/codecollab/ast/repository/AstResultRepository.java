package com.codecollab.ast.repository;

import com.codecollab.ast.dto.AnalysisStatus;
import com.codecollab.ast.entity.AstResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AstResultRepository extends JpaRepository<AstResult, Long> {

    Optional<AstResult> findBySnippetId(Long snippetId);

    boolean existsBySnippetId(Long snippetId);

    @Modifying
    @Query("DELETE FROM AstResult a WHERE a.snippetId = :snippetId")
    void deleteBySnippetId(@Param("snippetId") Long snippetId);

    Page<AstResult> findAllByStatus(AnalysisStatus status, Pageable pageable);
}
