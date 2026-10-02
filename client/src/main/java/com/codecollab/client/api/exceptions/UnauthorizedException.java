package com.codecollab.client.api.exceptions;

/** Thrown when the backend returns HTTP 401. Triggers re-login flow. */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException() {
        super(401, "Session expired. Please log in again.");
    }
}
