package com.codecollab.snippet.service.impl;

import com.codecollab.snippet.dto.SnippetSearchCriteria;
import com.codecollab.snippet.dto.SnippetSearchRequest;
import com.codecollab.snippet.dto.SnippetSummaryResponse;
import com.codecollab.snippet.mapper.SnippetMapper;
import com.codecollab.snippet.repository.SnippetSearchRepositoryCustom;
import com.codecollab.snippet.service.SnippetSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SnippetSearchServiceImpl implements SnippetSearchService {

    private static final String RECENT_SEARCHES_KEY = "search:recent";
    private static final int RECENT_SEARCHES_MAX = 100;

    private final SnippetSearchRepositoryCustom searchRepo;
    private final SnippetMapper snippetMapper;
    private final StringRedisTemplate redis;

    @Override
    public Page<SnippetSummaryResponse> search(SnippetSearchRequest request, Pageable pageable) {
        SnippetSearchCriteria criteria = SnippetSearchCriteria.builder()
                .query(request.query())
                .language(request.language())
                .ownerId(request.ownerId())
                .tagNames(request.tags())
                .build();

        Page<SnippetSummaryResponse> result = searchRepo.search(criteria, pageable)
                .map(snippetMapper::toSummary);

        // Track recent search term in Redis sorted set
        String q = request.query();
        if (q != null && !q.isBlank()) {
            try {
                redis.opsForZSet().incrementScore(RECENT_SEARCHES_KEY, q.trim().toLowerCase(), 1);
                // Trim to top RECENT_SEARCHES_MAX entries
                Long size = redis.opsForZSet().size(RECENT_SEARCHES_KEY);
                if (size != null && size > RECENT_SEARCHES_MAX) {
                    redis.opsForZSet().removeRange(RECENT_SEARCHES_KEY, 0, size - RECENT_SEARCHES_MAX - 1);
                }
            } catch (Exception e) {
                log.warn("Failed to track recent search in Redis: {}", e.getMessage());
            }
        }

        return result;
    }

    @Override
    public Page<SnippetSummaryResponse> searchWithFilters(
            String query, String language, Long ownerId,
            Set<String> tags, Pageable pageable) {

        SnippetSearchRequest request = new SnippetSearchRequest(query, language, ownerId, tags);
        return search(request, pageable);
    }

    @Override
    public List<String> getRecentSearches(int limit) {
        try {
            Set<String> terms = redis.opsForZSet()
                    .reverseRange(RECENT_SEARCHES_KEY, 0, limit - 1);
            if (terms == null) return Collections.emptyList();
            return List.copyOf(terms);
        } catch (Exception e) {
            log.warn("Failed to read recent searches from Redis: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
