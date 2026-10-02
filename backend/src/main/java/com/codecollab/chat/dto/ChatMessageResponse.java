package com.codecollab.chat.dto;

import com.codecollab.chat.entity.MessageType;
import java.time.Instant;

public record ChatMessageResponse(
    Long id,
    Long roomId,
    Long senderId,
    String senderUsername,
    Long recipientId,
    String recipientUsername,
    MessageType messageType,
    String content,
    boolean deleted,
    Instant createdAt
) {}
