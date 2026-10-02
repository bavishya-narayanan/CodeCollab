package com.codecollab.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRoomRequest(
    @NotBlank @Size(min = 2, max = 50)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Room name must be lowercase letters, digits, dashes")
    String name,

    @Size(max = 200)
    String description
) {}
