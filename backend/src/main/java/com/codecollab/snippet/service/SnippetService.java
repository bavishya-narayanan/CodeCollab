package com.codecollab.snippet.service;

import com.codecollab.common.PageResponse;
import com.codecollab.snippet.dto.CreateSnippetRequest;
import com.codecollab.snippet.dto.SnippetResponse;
import com.codecollab.snippet.dto.SnippetSummaryResponse;
import com.codecollab.snippet.dto.UpdateSnippetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SnippetService {

    SnippetResponse create(Long ownerId, CreateSnippetRequest request);

    SnippetResponse getById(Long id, Long viewerId);

    Page<SnippetSummaryResponse> listAll(Pageable pageable);

    Page<SnippetSummaryResponse> listByOwner(Long ownerId, Pageable pageable);

    SnippetResponse update(Long snippetId, Long actorId, UpdateSnippetRequest request);

    void delete(Long snippetId, Long actorId);

    long countByOwner(Long ownerId);
}
