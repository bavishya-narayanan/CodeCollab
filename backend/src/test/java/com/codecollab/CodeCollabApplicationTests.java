package com.codecollab;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import com.codecollab.config.JpaConfig;
import com.codecollab.health.HealthController;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test verifying the Spring MVC context loads without a real database or Redis.
 *
 * <p>Intentionally kept minimal — its sole purpose is to catch misconfigured beans
 * or missing component-scan paths that would prevent the application from starting.
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
class CodeCollabApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private com.codecollab.security.JwtService jwtService;

    @MockBean
    private com.codecollab.security.CustomUserDetailsService customUserDetailsService;

    @MockBean
    private com.codecollab.security.JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private com.codecollab.security.JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @Test
    @DisplayName("Spring MVC context loads successfully")
    @WithMockUser
    void contextLoads() throws Exception {
        // Simply verify that the /api/health endpoint is reachable — proves
        // the context wired up correctly without touching any real infrastructure.
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }
}
