package com.codecollab.chat.mapper;

import com.codecollab.chat.dto.RoomResponse;
import com.codecollab.chat.entity.Room;
import com.codecollab.chat.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomMapper {

    private final PresenceService presenceService;

    public RoomResponse toResponse(Room r) {
        long online = presenceService.countOnline(r.getId());
        return new RoomResponse(
            r.getId(), r.getName(), r.getDescription(),
            r.getCreatedBy(), r.isDefaultRoom(), online, r.getCreatedAt()
        );
    }
}
