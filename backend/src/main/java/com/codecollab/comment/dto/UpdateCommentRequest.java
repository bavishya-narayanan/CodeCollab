package com.codecollab.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(
    @NotBlank @Size(min = 1, max = 1000) String content
) {}
