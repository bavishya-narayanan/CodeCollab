package com.codecollab.chat.controller;

import com.codecollab.chat.dto.ChatMessageResponse;
import com.codecollab.chat.service.ChatService;
import com.codecollab.common.ApiResponse;
import com.codecollab.common.PageResponse;
import com.codecollab.security.CustomUserDetails;
import com.codecollab.user.entity.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat History")
public class ChatHistoryController {

    private final ChatService chatService;

    @GetMapping("/rooms/{roomId}/history")
    @Operation(summary = "Get room message history")
    public ApiResponse<PageResponse<ChatMessageResponse>> roomHistory(
            @PathVariable Long roomId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(chatService.history(roomId, pageable)));
    }

    @GetMapping("/dm/{userId}/history")
    @Operation(summary = "Get direct message history with another user")
    public ApiResponse<PageResponse<ChatMessageResponse>> directHistory(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(chatService.directHistory(principal.getUserId(), userId, pageable)));
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete a message")
    public ApiResponse<Void> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.name()));
        chatService.deleteMessage(messageId, principal.getUserId(), isAdmin);
        return ApiResponse.ok("Message deleted", null);
    }
}
