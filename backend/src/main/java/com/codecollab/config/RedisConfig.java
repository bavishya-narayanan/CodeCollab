package com.codecollab.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis template configuration.
 *
 * <p>StringRedisTemplate is auto-configured by Spring Boot and handles simple
 * String key/value operations. This bean adds a typed {@link RedisTemplate} that
 * serialises values as JSON — used in:
 * <ul>
 *   <li>Phase 3: result caching</li>
 *   <li>Phase 6: real-time presence tracking</li>
 * </ul>
 */
// Used in Phase 3 for caching, Phase 6 for presence
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Keys are plain strings — human-readable and easy to inspect with redis-cli
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Values are stored as JSON so they survive restarts and cross-service reads
        Jackson2JsonRedisSerializer<Object> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(buildObjectMapper(), Object.class);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // Triggers InitializingBean lifecycle — applies all serializers
        template.afterPropertiesSet();

        return template;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Shared ObjectMapper with Java-time support.
     * Dates are written as ISO-8601 strings, not Unix epoch numbers.
     */
    private ObjectMapper buildObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }
}
