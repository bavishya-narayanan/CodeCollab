package com.codecollab.snippet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * SnippetSearchRepositoryTest — integration tests using Testcontainers.
 * Disabled until Docker/WSL2 environment is available.
 */
@Disabled("Requires Testcontainers / Docker")
class SnippetSearchRepositoryTest {

    // Full integration tests for SnippetSearchRepositoryCustomImpl.
    // These require a real PostgreSQL instance with tsvector support.
    //
    // When Docker is available, implement with:
    //   @Testcontainers
    //   @DataJpaTest
    //   @AutoConfigureTestDatabase(replace = NONE)
    //
    // Test cases:
    //   1. search by "jwt" → matches title
    //   2. search by "token" → matches description or code
    //   3. search by non-existent term → empty page
    //   4. filter by language=JAVA → only java snippets
    //   5. filter by tag="jwt" → only tagged snippets
    //   6. filter by ownerId → only that user's snippets
    //   7. combined query + language + tag → intersection works
    //   8. ordering by ts_rank when query present
    //   9. ordering by createdAt when query absent

    @Test
    void placeholder_compilesAndRunsWithoutDocker() {
        // This test exists to ensure the class compiles without errors.
        // Real tests require a running Docker daemon with PostgreSQL.
    }
}
