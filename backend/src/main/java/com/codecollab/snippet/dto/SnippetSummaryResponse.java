package com.codecollab.snippet.dto;

import java.time.Instant;
import java.util.List;

public record SnippetSummaryResponse(
    Long id,
    Long ownerId,
    String ownerUsername,
    String title,
    String description,
    String language,
    Integer viewCount,
    List<String> tagNames,
    long voteScore,
    long commentCount,
    Instant createdAt,
    Instant updatedAt
) {}
