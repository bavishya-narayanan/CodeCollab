package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.Tag;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class TagApi {
    private final ApiClient http = ApiClient.getInstance();

    public List<Tag> listPopular() throws ApiException {
        return http.get("/api/tags/popular", new TypeReference<ApiResponse<List<Tag>>>() {});
    }

    public List<Tag> search(String prefix) throws ApiException {
        return http.get("/api/tags?prefix=" + prefix, new TypeReference<ApiResponse<List<Tag>>>() {});
    }
}
