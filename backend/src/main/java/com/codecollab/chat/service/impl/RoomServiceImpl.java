package com.codecollab.chat.service.impl;

import com.codecollab.chat.dto.CreateRoomRequest;
import com.codecollab.chat.dto.RoomResponse;
import com.codecollab.chat.entity.Room;
import com.codecollab.chat.exception.RoomAccessDeniedException;
import com.codecollab.chat.exception.RoomNotFoundException;
import com.codecollab.chat.mapper.RoomMapper;
import com.codecollab.chat.repository.RoomRepository;
import com.codecollab.chat.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms() {
        return roomRepository.findAllActiveOrdered().stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse createRoom(Long actorId, CreateRoomRequest req) {
        String name = req.name().toLowerCase().trim();
        if (roomRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Room name already exists");
        }

        Room room = Room.builder()
                .name(name)
                .description(req.description())
                .createdBy(actorId)
                .defaultRoom(false)
                .archived(false)
                .build();
        room = roomRepository.save(room);
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        if (room.isArchived()) {
            throw new RoomNotFoundException(roomId);
        }
        return roomMapper.toResponse(room);
    }

    @Override
    public void deleteRoom(Long roomId, Long actorId, boolean isAdmin) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));

        if (room.isDefaultRoom() && !isAdmin) {
            throw new RoomAccessDeniedException("Cannot delete default rooms");
        }

        if (!isAdmin && !room.getCreatedBy().equals(actorId)) {
            throw new RoomAccessDeniedException("Cannot delete rooms you didn't create");
        }

        room.setArchived(true);
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public Long resolveRoomIdByName(String name) {
        return roomRepository.findByNameIgnoreCase(name)
                .map(Room::getId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found: " + name));
    }
}
