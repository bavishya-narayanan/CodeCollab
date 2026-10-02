package com.codecollab.snippet.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateSnippetRequest(
    @Size(min = 3, max = 200)
    String title,

    @Size(max = 2000)
    String description,

    @Size(min = 1, max = 50000)
    String code,

    @Pattern(regexp = "^[A-Za-z+#-]{1,30}$")
    String language,

    @Size(max = 10)
    Set<@Size(min = 1, max = 50) String> tags
) {}
