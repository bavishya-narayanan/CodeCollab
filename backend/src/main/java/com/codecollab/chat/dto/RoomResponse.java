package com.codecollab.chat.dto;

import java.time.Instant;

public record RoomResponse(
    Long id,
    String name,
    String description,
    Long createdBy,
    boolean defaultRoom,
    long onlineCount,
    Instant createdAt
) {}
