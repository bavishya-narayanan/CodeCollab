package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.AuthResponse;
import com.codecollab.client.model.User;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class AuthApi {

    private final ApiClient http = ApiClient.getInstance();

    /** Returns the JWT token string. */
    public String login(String username, String password) throws ApiException {
        Map<String, String> body = Map.of("usernameOrEmail", username, "password", password);
        AuthResponse resp = http.post("/api/auth/login", body, new TypeReference<ApiResponse<AuthResponse>>() {});
        return resp.getAccessToken();
    }

    public User register(String username, String email, String password) throws ApiException {
        Map<String, String> body = Map.of("username", username, "email", email, "password", password);
        return http.post("/api/auth/register", body, new TypeReference<ApiResponse<User>>() {});
    }
}
