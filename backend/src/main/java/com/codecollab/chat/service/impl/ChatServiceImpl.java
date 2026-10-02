package com.codecollab.chat.service.impl;

import com.codecollab.chat.dto.ChatMessageRequest;
import com.codecollab.chat.dto.ChatMessageResponse;
import com.codecollab.chat.entity.ChatMessage;
import com.codecollab.chat.entity.MessageType;
import com.codecollab.chat.entity.Room;
import com.codecollab.chat.exception.RateLimitExceededException;
import com.codecollab.chat.exception.RoomNotFoundException;
import com.codecollab.chat.mapper.ChatMessageMapper;
import com.codecollab.chat.repository.ChatMessageRepository;
import com.codecollab.chat.repository.RoomRepository;
import com.codecollab.chat.service.ChatService;
import com.codecollab.config.RedisPubSubConfig;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private static final int RATE_LIMIT_MAX = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofSeconds(5);

    private final ChatMessageRepository messageRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ChatMessageMapper mapper;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Override
    public ChatMessageResponse sendToRoom(Long roomId, Long senderId, ChatMessageRequest req) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException(roomId));
        if (room.isArchived()) {
            throw new RoomNotFoundException(roomId);
        }

        checkRateLimit(senderId, roomId);

        ChatMessage msg = ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .messageType(MessageType.CHAT)
                .content(req.content())
                .build();
        msg = messageRepository.save(msg);

        ChatMessageResponse response = mapper.toResponse(msg);
        publish(RedisPubSubConfig.CHAT_CHANNEL_PREFIX + roomId, response);
        return response;
    }

    @Override
    public ChatMessageResponse sendDirect(Long recipientId, Long senderId, ChatMessageRequest req) {
        if (recipientId.equals(senderId)) {
            throw new IllegalArgumentException("Cannot DM yourself");
        }

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));
        if (recipient.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Recipient is not active");
        }

        // We can reuse the rate limiter by using 0 or negative for DM room id, 
        // or a different prefix. For now, use "dm:" + recipientId
        String key = "ratelimit:chat:dm:" + senderId + ":" + recipientId;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, RATE_LIMIT_WINDOW);
        }
        if (count != null && count > RATE_LIMIT_MAX) {
            throw new RateLimitExceededException();
        }

        ChatMessage msg = ChatMessage.builder()
                .senderId(senderId)
                .recipientId(recipientId)
                .messageType(MessageType.DM)
                .content(req.content())
                .build();
        msg = messageRepository.save(msg);

        ChatMessageResponse response = mapper.toResponse(msg);
        publish(RedisPubSubConfig.DM_CHANNEL_PREFIX + recipientId, response);
        publish(RedisPubSubConfig.DM_CHANNEL_PREFIX + senderId, response); // echo
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> history(Long roomId, Pageable pageable) {
        return messageRepository.findHistoryForRoom(roomId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> directHistory(Long userId, Long otherUserId, Pageable pageable) {
        return messageRepository.findDirectHistory(userId, otherUserId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    public void deleteMessage(Long messageId, Long actorId, boolean isAdmin) {
        ChatMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (!isAdmin && !msg.getSenderId().equals(actorId)) {
            throw new IllegalArgumentException("Cannot delete this message");
        }

        messageRepository.softDelete(messageId);
        // We could publish a delete event here if the UI supported dynamic removal
    }

    private void checkRateLimit(Long userId, Long roomId) {
        String key = "ratelimit:chat:" + userId + ":" + roomId;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, RATE_LIMIT_WINDOW);
        }
        if (count != null && count > RATE_LIMIT_MAX) {
            throw new RateLimitExceededException();
        }
    }

    private void publish(String channel, ChatMessageResponse response) {
        try {
            redis.convertAndSend(channel, objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize message", e);
        }
    }
}
