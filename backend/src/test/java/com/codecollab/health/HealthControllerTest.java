package com.codecollab.health;

import com.codecollab.common.GlobalExceptionHandler;
import com.codecollab.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Slice test for {@link HealthController}.
 *
 * <p>Uses {@code @WebMvcTest} so only the web layer is loaded — no DataSource,
 * no Hibernate, no Redis connection. All infrastructure beans are mocked.
 * Spring Security is satisfied via {@code @WithMockUser}.
 */
@WebMvcTest(
        controllers = HealthController.class,
        excludeAutoConfiguration = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class,
                RedisAutoConfiguration.class,
                RedisRepositoriesAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JpaConfig.class
        )
)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private GlobalExceptionHandler globalExceptionHandler;

    @MockBean
    private com.codecollab.security.JwtService jwtService;

    @MockBean
    private com.codecollab.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.codecollab.security.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private com.codecollab.security.JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // -------------------------------------------------------------------------
    // GET /api/health
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/health → 200 with status UP")
    @WithMockUser
    void getHealth_returns200WithStatusUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("UP")));
    }

    @Test
    @DisplayName("GET /api/health → response contains service=codecollab")
    @WithMockUser
    void getHealth_containsServiceName() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.service", is("codecollab")));
    }

    @Test
    @DisplayName("GET /api/health → response has success=true and timestamp")
    @WithMockUser
    void getHealth_hasSuccessTrueAndTimestamp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // -------------------------------------------------------------------------
    // GET /api/health/db
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/health/db → 200 when SELECT 1 succeeds")
    @WithMockUser
    void getDbHealth_returns200WhenDatabaseIsReachable() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        mockMvc.perform(get("/api/health/db"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Database connection OK")));
    }

    // -------------------------------------------------------------------------
    // GET /api/health/redis
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/health/redis → 200 when Redis round-trip succeeds")
    @WithMockUser
    @SuppressWarnings("unchecked")
    void getRedisHealth_returns200WhenRedisIsReachable() throws Exception {
        ValueOperations<String, String> valueOps =
                org.mockito.Mockito.mock(ValueOperations.class);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        doNothing().when(valueOps).set(anyString(), anyString(), any());
        when(valueOps.get("health:ping")).thenReturn("pong");

        mockMvc.perform(get("/api/health/redis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Redis connection OK")));
    }
}
