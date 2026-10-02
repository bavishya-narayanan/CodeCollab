package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.AstResult;
import com.fasterxml.jackson.core.type.TypeReference;

public class AstApi {
    private final ApiClient http = ApiClient.getInstance();

    public AstResult getAnalysis(Long snippetId) throws ApiException {
        return http.get("/api/snippets/" + snippetId + "/analysis",
            new TypeReference<ApiResponse<AstResult>>() {});
    }
}
