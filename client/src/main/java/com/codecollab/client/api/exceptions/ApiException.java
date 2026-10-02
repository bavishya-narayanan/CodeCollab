package com.codecollab.client.api.exceptions;

/** Thrown when the backend returns a non-2xx status code. */
public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() { return statusCode; }
}
