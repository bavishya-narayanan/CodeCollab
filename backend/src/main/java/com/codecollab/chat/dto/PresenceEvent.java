package com.codecollab.chat.dto;

public record PresenceEvent(
    Long roomId,
    PresencePayload payload
) {}
