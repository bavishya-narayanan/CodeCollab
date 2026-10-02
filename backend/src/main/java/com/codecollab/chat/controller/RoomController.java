package com.codecollab.chat.controller;

import com.codecollab.chat.dto.CreateRoomRequest;
import com.codecollab.chat.dto.RoomResponse;
import com.codecollab.chat.service.RoomService;
import com.codecollab.common.ApiResponse;
import com.codecollab.security.CustomUserDetails;
import com.codecollab.user.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "Chat Rooms")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "List all active chat rooms")
    public ApiResponse<List<RoomResponse>> listRooms() {
        return ApiResponse.ok(roomService.listRooms());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new chat room")
    public ApiResponse<RoomResponse> createRoom(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateRoomRequest req) {
        return ApiResponse.ok("Room created",
                roomService.createRoom(principal.getUserId(), req));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room details")
    public ApiResponse<RoomResponse> getRoom(@PathVariable Long id) {
        return ApiResponse.ok(roomService.getRoom(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete (archive) a chat room")
    public ApiResponse<Void> deleteRoom(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.name()));
        roomService.deleteRoom(id, principal.getUserId(), isAdmin);
        return ApiResponse.ok("Room deleted", null);
    }
}
