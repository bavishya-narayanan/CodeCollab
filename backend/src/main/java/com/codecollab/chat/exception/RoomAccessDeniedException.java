package com.codecollab.chat.exception;

public class RoomAccessDeniedException extends RuntimeException {
    public RoomAccessDeniedException(String message) {
        super(message);
    }
}
