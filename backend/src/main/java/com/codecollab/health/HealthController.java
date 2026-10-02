package com.codecollab.health;

import com.codecollab.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Infrastructure health-check endpoints.
 *
 * <p>These are <em>active</em> probes — they actually exercise the database
 * and Redis, unlike the Actuator's passive health indicators. Use them to
 * quickly confirm every layer of the stack is reachable after a deploy.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health", description = "Infrastructure connectivity probes")
public class HealthController {

    private static final String REDIS_PROBE_KEY = "health:ping";
    private static final String REDIS_PROBE_VALUE = "pong";

    // Constructor injection via @RequiredArgsConstructor — no @Autowired field injection
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    // -------------------------------------------------------------------------
    // Endpoints
    // -------------------------------------------------------------------------

    /**
     * Top-level liveness check.
     * Returns application metadata so you can confirm which version is running.
     */
    @GetMapping
    @Operation(
            summary = "Application liveness check",
            description = "Returns application version, phase, and a timestamp. No I/O performed."
    )
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> payload = Map.of(
                "status", "UP",
                "service", "codecollab",
                "version", "0.0.1-SNAPSHOT",
                "phase", "0-foundation",
                "timestamp", Instant.now()
        );
        return ApiResponse.ok(payload);
    }

    /**
     * Database connectivity probe.
     * Runs {@code SELECT 1} — the lightest possible query that still round-trips
     * through HikariCP, the JDBC driver, and Postgres.
     */
    @GetMapping("/db")
    @Operation(
            summary = "Database connectivity probe",
            description = "Runs SELECT 1 against PostgreSQL. Returns 200 if the connection pool is healthy."
    )
    public ApiResponse<String> dbHealth() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            log.debug("Database probe: OK");
            return ApiResponse.ok("Database connection OK", "SELECT 1 returned successfully");
        } catch (Exception ex) {
            log.error("Database probe failed: {}", ex.getMessage(), ex);
            return ApiResponse.error("Database connection failed: " + ex.getMessage());
        }
    }

    /**
     * Redis connectivity probe.
     * Writes a short-lived key and immediately reads it back to verify
     * both the write path and the read path are functional.
     */
    @GetMapping("/redis")
    @Operation(
            summary = "Redis connectivity probe",
            description = "Writes a TTL key and reads it back. Returns 200 if Redis is reachable and both operations succeed."
    )
    public ApiResponse<String> redisHealth() {
        try {
            // Write with a 10-second TTL so the probe key never accumulates
            stringRedisTemplate.opsForValue()
                    .set(REDIS_PROBE_KEY, REDIS_PROBE_VALUE, Duration.ofSeconds(10));

            String value = stringRedisTemplate.opsForValue().get(REDIS_PROBE_KEY);

            if (REDIS_PROBE_VALUE.equals(value)) {
                log.debug("Redis probe: OK");
                return ApiResponse.ok("Redis connection OK", "Read-write round-trip succeeded");
            } else {
                // This shouldn't happen, but guard against it explicitly
                log.warn("Redis probe: value mismatch — expected '{}', got '{}'", REDIS_PROBE_VALUE, value);
                return ApiResponse.error("Redis read-back mismatch — cache may be corrupted");
            }
        } catch (Exception ex) {
            log.error("Redis probe failed: {}", ex.getMessage(), ex);
            return ApiResponse.error("Redis connection failed: " + ex.getMessage());
        }
    }
}
