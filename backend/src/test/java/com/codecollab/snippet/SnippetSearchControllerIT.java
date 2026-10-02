package com.codecollab.snippet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * SnippetSearchControllerIT — integration tests using Testcontainers + MockMvc.
 * Disabled until Docker/WSL2 environment is available.
 */
@Disabled("Requires Testcontainers / Docker")
class SnippetSearchControllerIT {

    // Full integration tests for SnippetSearchController.
    //
    // Test cases:
    //   1. Create 3 snippets as user A, 2 as user B
    //   2. GET /api/snippets/search?q=some-term → returns matching
    //   3. GET /api/snippets/search?language=JAVA → returns java only
    //   4. GET /api/snippets/search?tag=jwt → returns tagged only
    //   5. GET /api/snippets/search?ownerId=<id> → returns owner's only
    //   6. GET /api/snippets/search?q=...&tag=...&language=... → combined
    //   7. GET /api/snippets/search → 200 with all (unfiltered)
    //   8. GET /api/snippets/search?q=nonexistent → empty page
    //   9. GET /api/snippets/search without token → 401
    //   10. GET /api/snippets/search/recent → returns recent terms
    //   11. Confirm /api/snippets/search does NOT collide with /api/snippets/{id}

    @Test
    void placeholder_compilesAndRunsWithoutDocker() {
        // This test exists to ensure the class compiles without errors.
    }
}
