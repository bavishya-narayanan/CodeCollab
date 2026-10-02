package com.codecollab.user.dto;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresInSeconds,
    UserResponse user
) {}
