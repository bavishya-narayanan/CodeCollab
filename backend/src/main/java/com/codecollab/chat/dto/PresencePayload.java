package com.codecollab.chat.dto;

import java.time.Instant;

public record PresencePayload(
    Long userId,
    String username,
    String action,      // "JOIN" | "LEAVE"
    long onlineCount,
    Instant at
) {}
