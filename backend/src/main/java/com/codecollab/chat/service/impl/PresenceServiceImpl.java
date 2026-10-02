package com.codecollab.chat.service.impl;

import com.codecollab.chat.dto.PresencePayload;
import com.codecollab.chat.service.PresenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceServiceImpl implements PresenceService {

    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration PRESENCE_TTL = Duration.ofSeconds(60);
    private static final String KEY_PREFIX = "presence:room:";
    private static final String CHANNEL_PREFIX = "presence:room:";

    @Override
    public void userJoined(Long roomId, Long userId, String username) {
        String key = KEY_PREFIX + roomId;
        redis.opsForHash().put(key, userId.toString(), username);
        redis.expire(key, PRESENCE_TTL);

        broadcast(roomId, userId, username, "JOIN");
    }

    @Override
    public void userLeft(Long roomId, Long userId, String username) {
        String key = KEY_PREFIX + roomId;
        redis.opsForHash().delete(key, userId.toString());

        broadcast(roomId, userId, username, "LEAVE");
    }

    @Override
    public long countOnline(Long roomId) {
        Long size = redis.opsForHash().size(KEY_PREFIX + roomId);
        return size == null ? 0 : size;
    }

    @Override
    public boolean isOnline(Long roomId, Long userId) {
        return redis.opsForHash().hasKey(KEY_PREFIX + roomId, userId.toString());
    }

    @Override
    public Set<Long> onlineUsers(Long roomId) {
        Set<Object> keys = redis.opsForHash().keys(KEY_PREFIX + roomId);
        return keys.stream()
            .map(k -> Long.valueOf(k.toString()))
            .collect(Collectors.toSet());
    }

    @Override
    public void refresh(Long roomId, Long userId, String username) {
        String key = KEY_PREFIX + roomId;
        redis.opsForHash().put(key, userId.toString(), username);
        redis.expire(key, PRESENCE_TTL);
    }

    private void broadcast(Long roomId, Long userId, String username, String action) {
        PresencePayload payload = new PresencePayload(
            userId, username, action, countOnline(roomId), Instant.now());
        try {
            redis.convertAndSend(
                CHANNEL_PREFIX + roomId,
                objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            log.error("Failed to broadcast presence", e);
        }
    }
}
