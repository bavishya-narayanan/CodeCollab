package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.Comment;
import com.codecollab.client.model.PageResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.Map;

public class CommentApi {
    private final ApiClient http = ApiClient.getInstance();

    public PageResponse<Comment> list(Long snippetId, int page, int size) throws ApiException {
        return http.get("/api/snippets/" + snippetId + "/comments?page=" + page + "&size=" + size,
            new TypeReference<ApiResponse<PageResponse<Comment>>>() {});
    }

    public Comment post(Long snippetId, String content, Long parentId) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("content", content);
        if (parentId != null) body.put("parentId", parentId);
        return http.post("/api/snippets/" + snippetId + "/comments", body,
            new TypeReference<ApiResponse<Comment>>() {});
    }

    public Comment edit(Long commentId, String content) throws ApiException {
        return http.put("/api/comments/" + commentId, Map.of("content", content),
            new TypeReference<ApiResponse<Comment>>() {});
    }

    public void delete(Long commentId) throws ApiException {
        http.delete("/api/comments/" + commentId);
    }
}
