package com.codecollab.user.dto;

import java.time.Instant;

public record UserResponse(
    Long id,
    String username,
    String email,
    String role,
    String status,
    Instant createdAt,
    Instant lastLoginAt
) {}
