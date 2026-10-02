package com.codecollab.chat.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long id) {
        super("Room not found: " + id);
    }
    public RoomNotFoundException(String message) {
        super(message);
    }
}
