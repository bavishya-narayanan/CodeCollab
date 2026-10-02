package com.codecollab.snippet.repository;

import com.codecollab.snippet.dto.SnippetSearchCriteria;
import com.codecollab.snippet.entity.Snippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SnippetSearchRepositoryCustom {
    Page<Snippet> search(SnippetSearchCriteria criteria, Pageable pageable);
}
