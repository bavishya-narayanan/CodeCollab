package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.api.exceptions.UnauthorizedException;
import com.codecollab.client.config.AppState;
import com.codecollab.client.config.ClientConfig;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.util.JsonMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Low-level HTTP wrapper around OkHttp.
 * All API classes delegate here.
 */
public final class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ApiClient INSTANCE = new ApiClient();

    private final OkHttpClient http;
    private final String       baseUrl;

    private ApiClient() {
        this.http = new OkHttpClient.Builder()
                .connectTimeout(ClientConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(ClientConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();
        this.baseUrl = ClientConfig.BASE_URL;
    }

    public static ApiClient getInstance() { return INSTANCE; }

    // ── request builders ───────────────────────────────────────────────────

    private Request.Builder builder(String path) {
        Request.Builder b = new Request.Builder().url(baseUrl + path);
        String token = AppState.getInstance().getToken();
        if (token != null) b.header("Authorization", "Bearer " + token);
        return b;
    }

    // ── public helpers ─────────────────────────────────────────────────────

    public <T> T get(String path, TypeReference<ApiResponse<T>> type) throws ApiException {
        Request req = builder(path).get().build();
        return execute(req, type);
    }

    public <T> T post(String path, Object body, TypeReference<ApiResponse<T>> type) throws ApiException {
        RequestBody rb = RequestBody.create(JsonMapper.toJson(body), JSON);
        Request req = builder(path).post(rb).build();
        return execute(req, type);
    }

    public <T> T put(String path, Object body, TypeReference<ApiResponse<T>> type) throws ApiException {
        RequestBody rb = RequestBody.create(JsonMapper.toJson(body), JSON);
        Request req = builder(path).put(rb).build();
        return execute(req, type);
    }

    public void delete(String path) throws ApiException {
        Request req = builder(path).delete().build();
        try (Response resp = http.newCall(req).execute()) {
            handleStatus(resp, "");
        } catch (IOException e) {
            throw new ApiException(0, "Network error: " + e.getMessage());
        }
    }

    // ── internal ───────────────────────────────────────────────────────────

    private <T> T execute(Request req, TypeReference<ApiResponse<T>> type) throws ApiException {
        try (Response resp = http.newCall(req).execute()) {
            String bodyStr = resp.body() != null ? resp.body().string() : "{}";
            handleStatus(resp, bodyStr);
            ApiResponse<T> envelope = JsonMapper.fromJson(bodyStr, type);
            return envelope.getData();
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new ApiException(0, "Network error: " + e.getMessage());
        }
    }

    private void handleStatus(Response resp, String body) throws ApiException {
        if (resp.code() == 401) throw new UnauthorizedException();
        if (!resp.isSuccessful()) {
            log.warn("HTTP {} from {}: {}", resp.code(), resp.request().url(), body);
            // Try to parse message from envelope
            String msg = body;
            try {
                ApiResponse<?> env = JsonMapper.fromJson(body, new TypeReference<ApiResponse<Object>>() {});
                if (env.getMessage() != null) msg = env.getMessage();
            } catch (Exception ignored) {
                log.warn("Failed to parse error envelope: {}", ignored.getMessage());
            }
            throw new ApiException(resp.code(), msg);
        }
    }
}
