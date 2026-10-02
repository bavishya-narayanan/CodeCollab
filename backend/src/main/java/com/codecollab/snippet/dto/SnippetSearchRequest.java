package com.codecollab.snippet.dto;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record SnippetSearchRequest(
    @Size(max = 200) String query,
    @Size(max = 30) String language,
    Long ownerId,
    @Size(max = 10) Set<@Size(min = 1, max = 50) String> tags
) {}
