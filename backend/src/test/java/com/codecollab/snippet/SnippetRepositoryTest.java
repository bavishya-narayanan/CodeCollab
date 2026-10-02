package com.codecollab.snippet;

import com.codecollab.snippet.entity.Snippet;
import com.codecollab.snippet.entity.SnippetLanguage;
import com.codecollab.snippet.repository.SnippetRepository;
import com.codecollab.user.entity.Role;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Disabled("Docker/WSL2 is not available in the current environment")
class SnippetRepositoryTest {

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
    private SnippetRepository snippetRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("snippet_owner");
        testUser.setEmail("owner@example.com");
        testUser.setPasswordHash("hash");
        testUser.setRole(Role.USER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.saveAndFlush(testUser);
    }

    @Test
    void testSaveAndFindById() {
        Snippet snippet = Snippet.builder()
                .owner(testUser)
                .title("Test Snippet")
                .code("System.out.println();")
                .language(SnippetLanguage.JAVA)
                .build();
        snippet = snippetRepository.saveAndFlush(snippet);

        Optional<Snippet> found = snippetRepository.findByIdAndDeletedFalse(snippet.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Snippet", found.get().getTitle());
    }

    @Test
    void testFindByIdAndDeletedFalse() {
        Snippet snippet = Snippet.builder()
                .owner(testUser)
                .title("Deleted Snippet")
                .code("code")
                .language(SnippetLanguage.PYTHON)
                .build();
        snippet.softDelete();
        snippet = snippetRepository.saveAndFlush(snippet);

        Optional<Snippet> found = snippetRepository.findByIdAndDeletedFalse(snippet.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindAllByOwnerIdAndDeletedFalse() {
        Snippet snippet = Snippet.builder()
                .owner(testUser)
                .title("My Snippet")
                .code("code")
                .language(SnippetLanguage.JAVA)
                .build();
        snippetRepository.saveAndFlush(snippet);

        Page<Snippet> page = snippetRepository.findAllByOwnerIdAndDeletedFalse(testUser.getId(), PageRequest.of(0, 10));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void testIncrementViewCount() {
        Snippet snippet = Snippet.builder()
                .owner(testUser)
                .title("View Snippet")
                .code("code")
                .language(SnippetLanguage.JAVA)
                .build();
        snippet = snippetRepository.saveAndFlush(snippet);

        snippetRepository.incrementViewCount(snippet.getId());
        snippetRepository.flush();

        Snippet updated = snippetRepository.findById(snippet.getId()).orElseThrow();
        assertEquals(1, updated.getViewCount());
    }

    @Test
    void testCountByOwnerIdAndDeletedFalse() {
        Snippet snippet = Snippet.builder()
                .owner(testUser)
                .title("Count Snippet")
                .code("code")
                .language(SnippetLanguage.JAVA)
                .build();
        snippetRepository.saveAndFlush(snippet);

        long count = snippetRepository.countByOwnerIdAndDeletedFalse(testUser.getId());
        assertEquals(1, count);
    }
}
