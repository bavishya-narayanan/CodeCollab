package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.PageResponse;
import com.codecollab.client.model.Snippet;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnippetApi {

    private final ApiClient http = ApiClient.getInstance();

    public PageResponse<Snippet> list(int page, int size) throws ApiException {
        return http.get(
            "/api/snippets?page=" + page + "&size=" + size,
            new TypeReference<ApiResponse<PageResponse<Snippet>>>() {});
    }

    public PageResponse<Snippet> mySnippets(int page, int size) throws ApiException {
        return http.get(
            "/api/snippets/mine?page=" + page + "&size=" + size,
            new TypeReference<ApiResponse<PageResponse<Snippet>>>() {});
    }

    public PageResponse<Snippet> search(String query, String language, String tag, int page, int size) throws ApiException {
        StringBuilder url = new StringBuilder("/api/snippets/search?page=" + page + "&size=" + size);
        if (query    != null && !query.isBlank())    url.append("&q=").append(encode(query));
        if (language != null && !language.isBlank()) url.append("&language=").append(encode(language));
        if (tag      != null && !tag.isBlank())      url.append("&tag=").append(encode(tag));
        return http.get(url.toString(), new TypeReference<ApiResponse<PageResponse<Snippet>>>() {});
    }

    public Snippet getById(Long id) throws ApiException {
        return http.get("/api/snippets/" + id,
            new TypeReference<ApiResponse<Snippet>>() {});
    }

    public Snippet create(String title, String description, String language,
                          String content, List<String> tags) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("language", language);
        body.put("code", content);
        body.put("tags", tags);
        return http.post("/api/snippets", body, new TypeReference<ApiResponse<Snippet>>() {});
    }

    public Snippet update(Long id, String title, String description, String language,
                          String content, List<String> tags) throws ApiException {
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("description", description);
        body.put("language", language);
        body.put("code", content);
        body.put("tags", tags);
        return http.put("/api/snippets/" + id, body, new TypeReference<ApiResponse<Snippet>>() {});
    }

    public void delete(Long id) throws ApiException {
        http.delete("/api/snippets/" + id);
    }

    private String encode(String s) {
        return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}
