package com.codecollab.snippet.service;

import com.codecollab.snippet.dto.SnippetSearchRequest;
import com.codecollab.snippet.dto.SnippetSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface SnippetSearchService {

    Page<SnippetSummaryResponse> search(SnippetSearchRequest request, Pageable pageable);

    Page<SnippetSummaryResponse> searchWithFilters(
        String query, String language, Long ownerId,
        Set<String> tags, Pageable pageable);

    List<String> getRecentSearches(int limit);
}
