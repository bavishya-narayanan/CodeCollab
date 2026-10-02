package com.codecollab.snippet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateSnippetRequest(
    @NotBlank @Size(min = 3, max = 200)
    String title,

    @Size(max = 2000)
    String description,

    @NotBlank @Size(min = 1, max = 50000)
    String code,

    @NotBlank
    @Pattern(regexp = "^[A-Za-z+#-]{1,30}$", message = "Invalid language identifier")
    String language,

    @Size(max = 10)
    Set<@Size(min = 1, max = 50) String> tags
) {}
