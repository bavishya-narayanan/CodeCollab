package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.Snippet;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class VoteApi {
    private final ApiClient http = ApiClient.getInstance();

    /** voteType: "UP", "DOWN", or "NONE" (remove vote) */
    public Snippet vote(Long snippetId, String voteType) throws ApiException {
        return http.put("/api/snippets/" + snippetId + "/vote",
            Map.of("type", voteType),
            new TypeReference<ApiResponse<Snippet>>() {});
    }
}
