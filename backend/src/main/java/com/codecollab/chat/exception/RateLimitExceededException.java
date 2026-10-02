package com.codecollab.chat.exception;

public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException() {
        super("Rate limit exceeded for chat messages");
    }
}
