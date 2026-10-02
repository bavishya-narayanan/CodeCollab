package com.codecollab.snippet.repository;

import com.codecollab.snippet.dto.SnippetSearchCriteria;
import com.codecollab.snippet.entity.Snippet;
import com.codecollab.tag.entity.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class SnippetSearchRepositoryCustomImpl implements SnippetSearchRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public Page<Snippet> search(SnippetSearchCriteria criteria, Pageable pageable) {
        String query = (criteria.query() != null && !criteria.query().isBlank())
                ? criteria.query().trim() : null;
        String language = criteria.language();
        Long ownerId = criteria.ownerId();
        Set<String> tagNames = (criteria.tagNames() != null && !criteria.tagNames().isEmpty())
                ? criteria.tagNames() : Collections.emptySet();
        int tagCount = tagNames.size();

        // Build the main SELECT SQL
        StringBuilder sql = new StringBuilder("SELECT DISTINCT s.* FROM snippets s ");
        if (tagCount > 0) {
            sql.append("JOIN snippet_tags st ON st.snippet_id = s.id ")
               .append("JOIN tags t ON t.id = st.tag_id ");
        }
        sql.append("WHERE s.deleted = FALSE ");

        Map<String, Object> params = new HashMap<>();
        if (query != null) {
            sql.append("AND s.search_vector @@ plainto_tsquery('english', :q) ");
            params.put("q", query);
        }
        if (language != null && !language.isBlank()) {
            sql.append("AND s.language = :lang ");
            params.put("lang", language.toUpperCase());
        }
        if (ownerId != null) {
            sql.append("AND s.user_id = :ownerId ");
            params.put("ownerId", ownerId);
        }
        if (tagCount > 0) {
            sql.append("AND t.name IN (:tagNames) ");
            params.put("tagNames", tagNames);
        }
        if (tagCount > 0) {
            sql.append("GROUP BY s.id ");
            sql.append("HAVING COUNT(DISTINCT t.id) = :tagCount ");
            params.put("tagCount", (long) tagCount);
        }

        // ORDER BY
        if (query != null) {
            sql.append("ORDER BY ts_rank(s.search_vector, plainto_tsquery('english', :q)) DESC, s.created_at DESC ");
        } else {
            sql.append("ORDER BY s.created_at DESC ");
        }

        // Build COUNT SQL (wrap as subquery)
        String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS _count";

        // Main query
        Query mainQuery = em.createNativeQuery(sql.toString(), Snippet.class);
        params.forEach(mainQuery::setParameter);
        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        List<Snippet> snippets = mainQuery.getResultList();

        // Count query
        Query countQuery = em.createNativeQuery(countSql);
        params.forEach(countQuery::setParameter);
        // Remove pagination params from count
        Number total = (Number) countQuery.getSingleResult();

        // Eagerly load tags for the fetched snippets to avoid LazyInitializationException
        if (!snippets.isEmpty()) {
            List<Long> ids = snippets.stream().map(Snippet::getId).collect(Collectors.toList());

            // Fetch tags as a batch: SELECT snippet_id, tag FROM snippet_tags JOIN tags
            List<Object[]> tagRows = em.createNativeQuery(
                    "SELECT st.snippet_id, t.id, t.name, t.usage_count, t.created_at " +
                    "FROM snippet_tags st JOIN tags t ON t.id = st.tag_id " +
                    "WHERE st.snippet_id IN (:ids)")
                    .setParameter("ids", ids)
                    .getResultList();

            Map<Long, Set<Tag>> tagsBySnippetId = new HashMap<>();
            for (Object[] row : tagRows) {
                Long snippetId = ((Number) row[0]).longValue();
                Tag tag = new Tag();
                tag.setId(((Number) row[1]).longValue());
                tag.setName((String) row[2]);
                tag.setUsageCount(((Number) row[3]).intValue());
                tagsBySnippetId.computeIfAbsent(snippetId, k -> new java.util.HashSet<>()).add(tag);
            }

            snippets.forEach(s -> {
                Set<Tag> snipTags = tagsBySnippetId.getOrDefault(s.getId(), Collections.emptySet());
                s.getTags().clear();
                s.getTags().addAll(snipTags);
            });
        }

        return new PageImpl<>(snippets, pageable, total.longValue());
    }
}
