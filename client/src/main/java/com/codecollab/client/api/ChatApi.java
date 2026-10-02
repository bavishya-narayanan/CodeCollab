package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.ChatMessage;
import com.codecollab.client.model.PageResponse;
import com.fasterxml.jackson.core.type.TypeReference;

public class ChatApi {
    private final ApiClient http = ApiClient.getInstance();

    public PageResponse<ChatMessage> roomHistory(Long roomId, int page, int size) throws ApiException {
        return http.get("/api/chat/rooms/" + roomId + "/history?page=" + page + "&size=" + size,
            new TypeReference<ApiResponse<PageResponse<ChatMessage>>>() {});
    }

    public PageResponse<ChatMessage> dmHistory(Long userId, int page, int size) throws ApiException {
        return http.get("/api/chat/dm/" + userId + "/history?page=" + page + "&size=" + size,
            new TypeReference<ApiResponse<PageResponse<ChatMessage>>>() {});
    }

    public void deleteMessage(Long messageId) throws ApiException {
        http.delete("/api/chat/messages/" + messageId);
    }
}
