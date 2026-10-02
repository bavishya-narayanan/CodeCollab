package com.codecollab.vote.repository;

import com.codecollab.vote.entity.Vote;
import com.codecollab.vote.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findBySnippetIdAndUserId(Long snippetId, Long userId);

    long countBySnippetIdAndVoteType(Long snippetId, VoteType type);

    @Query(value = """
        SELECT COALESCE(SUM(vote_type), 0) FROM votes
        WHERE snippet_id = :snippetId
        """, nativeQuery = true)
    Integer sumVoteTypeBySnippetId(@Param("snippetId") Long snippetId);

    @Query(value = """
        SELECT snippet_id AS snippetId, COALESCE(SUM(vote_type), 0) AS score
        FROM votes
        WHERE snippet_id IN :snippetIds
        GROUP BY snippet_id
        """, nativeQuery = true)
    List<SnippetScore> scoresBySnippetIds(@Param("snippetIds") List<Long> snippetIds);

    interface SnippetScore {
        Long getSnippetId();
        Long getScore();
    }
}
