package com.codecollab.chat.controller;

import com.codecollab.chat.dto.ChatMessageRequest;
import com.codecollab.chat.service.ChatService;
import com.codecollab.chat.service.PresenceService;
import com.codecollab.security.CustomUserDetails;
import com.codecollab.user.entity.User;
import com.codecollab.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final PresenceService presenceService;
    private final UserRepository userRepository;

    // Client sends to /app/chat/room/{roomId}
    @MessageMapping("/chat/room/{roomId}")
    public void handleRoomMessage(@DestinationVariable Long roomId,
                                   @Payload ChatMessageRequest req,
                                   Principal principal) {
        Long userId = extractUserId(principal);
        chatService.sendToRoom(roomId, userId, req);
    }

    // Client sends to /app/chat/dm/{recipientId}
    @MessageMapping("/chat/dm/{recipientId}")
    public void handleDirectMessage(@DestinationVariable Long recipientId,
                                     @Payload ChatMessageRequest req,
                                     Principal principal) {
        Long senderId = extractUserId(principal);
        chatService.sendDirect(recipientId, senderId, req);
    }

    // Client sends to /app/presence/join/{roomId}
    @MessageMapping("/presence/join/{roomId}")
    public void handleJoin(@DestinationVariable Long roomId, Principal principal) {
        Long userId = extractUserId(principal);
        String username = getUsername(userId);
        presenceService.userJoined(roomId, userId, username);
    }

    // Client sends to /app/presence/leave/{roomId}
    @MessageMapping("/presence/leave/{roomId}")
    public void handleLeave(@DestinationVariable Long roomId, Principal principal) {
        Long userId = extractUserId(principal);
        String username = getUsername(userId);
        presenceService.userLeft(roomId, userId, username);
    }

    // Heartbeat — refresh TTL
    @MessageMapping("/presence/heartbeat/{roomId}")
    public void heartbeat(@DestinationVariable Long roomId, Principal principal) {
        Long userId = extractUserId(principal);
        presenceService.refresh(roomId, userId, getUsername(userId));
    }

    private Long extractUserId(Principal principal) {
        CustomUserDetails ud = (CustomUserDetails) 
            ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        return ud.getUserId();
    }

    private String getUsername(Long userId) {
        return userRepository.findById(userId)
            .map(User::getUsername).orElse("[unknown]");
    }
}
