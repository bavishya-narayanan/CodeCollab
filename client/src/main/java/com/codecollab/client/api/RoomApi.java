package com.codecollab.client.api;

import com.codecollab.client.api.exceptions.ApiException;
import com.codecollab.client.model.ApiResponse;
import com.codecollab.client.model.Room;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public class RoomApi {
    private final ApiClient http = ApiClient.getInstance();

    public List<Room> list() throws ApiException {
        return http.get("/api/chat/rooms", new TypeReference<ApiResponse<List<Room>>>() {});
    }

    public Room create(String name, String description) throws ApiException {
        return http.post("/api/chat/rooms",
            Map.of("name", name, "description", description == null ? "" : description),
            new TypeReference<ApiResponse<Room>>() {});
    }

    public void delete(Long roomId) throws ApiException {
        http.delete("/api/chat/rooms/" + roomId);
    }
}
