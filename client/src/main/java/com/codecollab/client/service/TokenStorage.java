package com.codecollab.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persists the JWT token to a local file so the user stays logged in
 * between application launches.
 */
public final class TokenStorage {

    private static final Logger log  = LoggerFactory.getLogger(TokenStorage.class);
    private static final Path   FILE = Path.of(System.getProperty("user.home"), ".codecollab", "session.token");

    private TokenStorage() {}

    public static void save(String token) {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, token);
        } catch (IOException e) {
            log.warn("Could not persist token: {}", e.getMessage());
        }
    }

    public static String load() {
        try {
            if (Files.exists(FILE)) {
                String token = Files.readString(FILE).trim();
                return token.isBlank() ? null : token;
            }
        } catch (IOException e) {
            log.warn("Could not load token: {}", e.getMessage());
        }
        return null;
    }

    public static void clear() {
        try {
            Files.deleteIfExists(FILE);
        } catch (IOException e) {
            log.warn("Could not clear token: {}", e.getMessage());
        }
    }
}
