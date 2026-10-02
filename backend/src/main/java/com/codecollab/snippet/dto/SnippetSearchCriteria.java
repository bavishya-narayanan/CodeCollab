package com.codecollab.snippet.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record SnippetSearchCriteria(
    String query,        // may be null — triggers full-text search when present
    String language,     // enum name, may be null
    Long ownerId,        // may be null
    Set<String> tagNames // may be null or empty
) {}
