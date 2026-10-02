package com.codecollab.chat.listener;

import com.codecollab.chat.dto.ChatMessageResponse;
import com.codecollab.chat.dto.PresencePayload;
import com.codecollab.config.RedisPubSubConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisChatSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            if (channel.startsWith(RedisPubSubConfig.CHAT_CHANNEL_PREFIX)) {
                Long roomId = parseId(channel, 
                    RedisPubSubConfig.CHAT_CHANNEL_PREFIX);
                ChatMessageResponse resp = objectMapper
                    .readValue(body, ChatMessageResponse.class);
                messagingTemplate.convertAndSend(
                    "/topic/chat/room/" + roomId, resp);

            } else if (channel.startsWith(
                    RedisPubSubConfig.PRESENCE_CHANNEL_PREFIX)) {
                Long roomId = parseId(channel,
                    RedisPubSubConfig.PRESENCE_CHANNEL_PREFIX);
                PresencePayload payload = objectMapper
                    .readValue(body, PresencePayload.class);
                messagingTemplate.convertAndSend(
                    "/topic/presence/room/" + roomId, payload);

            } else if (channel.startsWith(
                    RedisPubSubConfig.DM_CHANNEL_PREFIX)) {
                Long userId = parseId(channel,
                    RedisPubSubConfig.DM_CHANNEL_PREFIX);
                ChatMessageResponse resp = objectMapper
                    .readValue(body, ChatMessageResponse.class);
                messagingTemplate.convertAndSendToUser(
                    userId.toString(), "/queue/dm", resp);
            }
        } catch (Exception ex) {
            log.error("Redis pubsub handling failed on {}: {}", 
                      channel, ex.getMessage());
        }
    }

    private Long parseId(String channel, String prefix) {
        return Long.valueOf(channel.substring(prefix.length()));
    }
}
