package com.codecollab.security;

import com.codecollab.user.entity.Role;
import com.codecollab.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-must-be-at-least-32-chars");
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 3600L);
        ReflectionTestUtils.setField(jwtService, "issuer", "codecollab-test");
    }

    @Test
    void testTokenGenerationAndExtraction() {
        User user = new User();
        user.setId(123L);
        user.setUsername("testuser");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);
        
        assertTrue(jwtService.isValid(token));
        assertEquals(123L, jwtService.extractUserId(token));
        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals("USER", jwtService.extractRole(token));
    }

    @Test
    void testExpiredToken() throws InterruptedException {
        ReflectionTestUtils.setField(jwtService, "expirationSeconds", 0L); // expires immediately
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);
        
        Thread.sleep(100); // ensure time passes
        
        assertFalse(jwtService.isValid(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.isValid("invalid-token-string"));
    }
}
