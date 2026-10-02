package com.codecollab.chat.entity;

public enum MessageType {
    CHAT,       // normal user message (persisted)
    DM,         // direct message (persisted, recipient_id set)
    JOIN,       // ephemeral, not persisted
    LEAVE,      // ephemeral, not persisted
    SYSTEM      // ephemeral system notice
}
