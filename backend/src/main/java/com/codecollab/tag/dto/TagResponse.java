package com.codecollab.tag.dto;

public record TagResponse(
    Long id,
    String name,
    Integer usageCount
) {}
