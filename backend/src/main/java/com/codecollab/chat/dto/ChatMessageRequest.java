package com.codecollab.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequest(
    @NotBlank @Size(min = 1, max = 2000)
    String content
) {}
