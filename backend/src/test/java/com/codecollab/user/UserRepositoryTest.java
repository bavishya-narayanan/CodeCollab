package com.codecollab.user;

import com.codecollab.user.entity.Role;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Disabled("Docker/WSL2 is not available in the current environment")
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("codecollab_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByUsernameIgnoreCase() {
        User user = new User();
        user.setUsername("TestUser");
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        assertTrue(userRepository.findByUsernameIgnoreCase("testuser").isPresent());
    }

    @Test
    void testExistsByEmailIgnoreCase() {
        User user = new User();
        user.setUsername("user2");
        user.setEmail("TEST2@example.com");
        user.setPasswordHash("hash");
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        assertTrue(userRepository.existsByEmailIgnoreCase("test2@example.com"));
    }

    @Test
    void testUniqueConstraintOnUsername() {
        User u1 = new User();
        u1.setUsername("duplicate");
        u1.setEmail("email1@example.com");
        u1.setPasswordHash("hash");
        u1.setRole(Role.USER);
        u1.setStatus(UserStatus.ACTIVE);
        userRepository.saveAndFlush(u1);

        User u2 = new User();
        u2.setUsername("duplicate");
        u2.setEmail("email2@example.com");
        u2.setPasswordHash("hash");
        u2.setRole(Role.USER);
        u2.setStatus(UserStatus.ACTIVE);

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.saveAndFlush(u2));
    }
}
