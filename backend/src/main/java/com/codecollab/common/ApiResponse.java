package com.codecollab.common;

import java.time.Instant;

/**
 * Uniform API response envelope used by every controller in CodeCollab.
 *
 * <p>Using a Java 17 record keeps this class immutable and boilerplate-free.
 * Every response — success or error — is wrapped in this type so clients
 * always have a consistent contract to parse.
 *
 * @param <T>       the type of the response payload
 * @param success   {@code true} for 2xx responses, {@code false} for errors
 * @param message   human-readable summary (required for errors, optional for ok)
 * @param data      the actual payload; may be {@code null} for errors
 * @param timestamp UTC instant at which the response was constructed
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {

    // -------------------------------------------------------------------------
    // Factory methods — callers never construct this directly
    // -------------------------------------------------------------------------

    /**
     * Success response with data and no extra message.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data, Instant.now());
    }

    /**
     * Success response with an explicit message and data payload.
     * Useful when the message itself is meaningful (e.g., "Database connection OK").
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }

    /**
     * Error response with a human-readable message and no payload.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, Instant.now());
    }
}
