package com.codecollab.ast.controller;

import com.codecollab.snippet.dto.CreateSnippetRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Disabled("Docker/WSL2 is not available in the current environment")
class AstControllerIT {

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

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = registerAndLogin("ast_user", "ast@example.com");
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
    void testAstAnalysisLifecycle() throws Exception {
        String badCode = """
            public class GodClass {
                public void method1(int a, int b, int c, int d, int e, int f) {
                    if (a > 42) {
                        try {} catch (Exception ex) {}
                    }
                }
            }
        """;

        CreateSnippetRequest createReq = new CreateSnippetRequest("Ast Test", "Description", badCode, "JAVA", null);
        
        MvcResult createResult = mockMvc.perform(post("/api/snippets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();
                
        Integer snippetIdInt = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");
        Long snippetId = snippetIdInt.longValue();

        // Wait a bit for the @Async analysis to finish
        Thread.sleep(1500);

        mockMvc.perform(get("/api/snippets/" + snippetId + "/analysis")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ANALYZED"))
                .andExpect(jsonPath("$.data.issues").isNotEmpty());
    }

    @Test
    void testPythonUnsupported() throws Exception {
        CreateSnippetRequest createReq = new CreateSnippetRequest("Python Snippet", "Description", "print('hello')", "PYTHON", null);
        
        MvcResult createResult = mockMvc.perform(post("/api/snippets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();
                
        Integer snippetIdInt = JsonPath.read(createResult.getResponse().getContentAsString(), "$.data.id");
        Long snippetId = snippetIdInt.longValue();

        Thread.sleep(1000);

        mockMvc.perform(get("/api/snippets/" + snippetId + "/analysis")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNSUPPORTED"));
    }
}
