package com.codecollab.snippet;

import com.codecollab.snippet.dto.CreateSnippetRequest;
import com.codecollab.snippet.dto.UpdateSnippetRequest;
import com.codecollab.user.dto.LoginRequest;
import com.codecollab.user.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Disabled("Docker/WSL2 is not available in the current environment")
class SnippetControllerIT {

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
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenOwner;
    private String tokenOther;

    @BeforeEach
    void setUp() throws Exception {
        tokenOwner = registerAndLogin("owneruser", "owner@example.com");
        tokenOther = registerAndLogin("otheruser", "other@example.com");
    }

    private String registerAndLogin(String username, String email) throws Exception {
        RegisterRequest registerReq = new RegisterRequest(username, email, "Password123!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerReq)));

        LoginRequest loginReq = new LoginRequest(username, "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        return JsonPath.read(loginResult.getResponse().getContentAsString(), "$.data.accessToken");
    }

    @Test
    void testSnippetLifecycle() throws Exception {
        // 1. POST without token -> 401
        CreateSnippetRequest createReq = new CreateSnippetRequest("title", "desc", "code", "java", null);
        mockMvc.perform(post("/api/snippets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isUnauthorized());

        // 2. POST with valid token -> 201
        MvcResult createResult = mockMvc.perform(post("/api/snippets")
                .header("Authorization", "Bearer " + tokenOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("title"))
                .andReturn();
        
        Integer snippetIdInt = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");
        Long snippetId = snippetIdInt.longValue();

        // 3. POST with invalid body (blank title) -> 400
        CreateSnippetRequest invalidReq = new CreateSnippetRequest("", "desc", "code", "java", null);
        mockMvc.perform(post("/api/snippets")
                .header("Authorization", "Bearer " + tokenOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());

        // 4. GET /api/snippets/{id} by owner -> 200, viewCount = 0
        mockMvc.perform(get("/api/snippets/" + snippetId)
                .header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(0));

        // 5. GET /api/snippets/{id} by other user -> increments view count
        mockMvc.perform(get("/api/snippets/" + snippetId)
                .header("Authorization", "Bearer " + tokenOther))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.viewCount").value(1));

        // 6. GET /api/snippets?size=5
        mockMvc.perform(get("/api/snippets?size=5")
                .header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(snippetId));

        // 7. GET /api/snippets/mine for other user -> empty
        mockMvc.perform(get("/api/snippets/mine")
                .header("Authorization", "Bearer " + tokenOther))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());

        // 8. PUT /api/snippets/{id} as other user -> 403
        UpdateSnippetRequest updateReq = new UpdateSnippetRequest("new title", null, null, null, null);
        mockMvc.perform(put("/api/snippets/" + snippetId)
                .header("Authorization", "Bearer " + tokenOther)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        // 9. PUT /api/snippets/{id} as owner -> 200
        mockMvc.perform(put("/api/snippets/" + snippetId)
                .header("Authorization", "Bearer " + tokenOwner)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("new title"));

        // 10. DELETE /api/snippets/{id} as other user -> 403
        mockMvc.perform(delete("/api/snippets/" + snippetId)
                .header("Authorization", "Bearer " + tokenOther))
                .andExpect(status().isForbidden());

        // 11. DELETE /api/snippets/{id} as owner -> 200
        mockMvc.perform(delete("/api/snippets/" + snippetId)
                .header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isOk());

        // 12. GET /api/snippets/{id} -> 404
        mockMvc.perform(get("/api/snippets/" + snippetId)
                .header("Authorization", "Bearer " + tokenOwner))
                .andExpect(status().isNotFound());
    }
}
