package com.codecollab.config;

import com.codecollab.chat.listener.RedisChatSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    public static final String CHAT_CHANNEL_PREFIX = "chat:room:";
    public static final String PRESENCE_CHANNEL_PREFIX = "presence:room:";
    public static final String DM_CHANNEL_PREFIX = "chat:user:";

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory factory,
            RedisChatSubscriber subscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);

        // Pattern: subscribe to all chat rooms
        container.addMessageListener(subscriber,
            new PatternTopic(CHAT_CHANNEL_PREFIX + "*"));
        container.addMessageListener(subscriber,
            new PatternTopic(PRESENCE_CHANNEL_PREFIX + "*"));
        container.addMessageListener(subscriber,
            new PatternTopic(DM_CHANNEL_PREFIX + "*"));

        return container;
    }
}
