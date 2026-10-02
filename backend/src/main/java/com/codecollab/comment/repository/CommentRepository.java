package com.codecollab.comment.repository;

import com.codecollab.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByIdAndDeletedFalse(Long id);

    Page<Comment> findAllBySnippetIdAndParentIdIsNullAndDeletedFalse(Long snippetId, Pageable pageable);

    Page<Comment> findAllByParentIdAndDeletedFalse(Long parentId, Pageable pageable);

    long countBySnippetIdAndDeletedFalse(Long snippetId);

    long countByParentIdAndDeletedFalse(Long parentId);

    @Query("""
        SELECT c.snippetId AS snippetId, COUNT(c) AS cnt
        FROM Comment c
        WHERE c.snippetId IN :snippetIds AND c.deleted = FALSE
        GROUP BY c.snippetId
        """)
    List<SnippetCommentCount> countBySnippetIds(@Param("snippetIds") List<Long> snippetIds);

    interface SnippetCommentCount {
        Long getSnippetId();
        Long getCnt();
    }
}
