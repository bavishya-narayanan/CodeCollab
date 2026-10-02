package com.codecollab.client.config;

/** Central configuration constants for the client. */
public final class ClientConfig {

    private ClientConfig() {}

    public static final String BASE_URL    = "http://localhost:8080";
    public static final String WS_BASE_URL = "ws://localhost:8080/ws";

    public static final int CONNECT_TIMEOUT_SECONDS = 10;
    public static final int READ_TIMEOUT_SECONDS    = 30;

    // Default pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
}
