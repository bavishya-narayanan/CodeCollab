package com.codecollab.chat.service;

import com.codecollab.chat.dto.CreateRoomRequest;
import com.codecollab.chat.dto.RoomResponse;

import java.util.List;

public interface RoomService {

    List<RoomResponse> listRooms();

    RoomResponse createRoom(Long actorId, CreateRoomRequest req);

    RoomResponse getRoom(Long roomId);

    void deleteRoom(Long roomId, Long actorId, boolean isAdmin);

    Long resolveRoomIdByName(String name);
}
