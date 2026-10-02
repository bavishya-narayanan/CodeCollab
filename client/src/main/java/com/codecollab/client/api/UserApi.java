package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.User;
import com.fasterxml.jackson.core.type.TypeReference;

public class UserApi {

    private final ApiClient http = ApiClient.getInstance();

    public User me() throws ApiException {
        return http.get("/api/users/me", new TypeReference<ApiResponse<User>>() {});
    }
}
