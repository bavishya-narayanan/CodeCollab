package com.codecollab.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 *
 * <p>Defines the API metadata shown at {@code /swagger-ui.html} and registers
 * a JWT bearer security scheme so that, when Phase 1 auth is added, the Swagger
 * UI will automatically display the "Authorize" button without any further config.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI codecollabOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", buildBearerScheme()));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Info buildApiInfo() {
        return new Info()
                .title("CodeCollab API")
                .description("Developer collaboration platform for code snippets")
                .version("1.0.0")
                .contact(new Contact()
                        .name("CodeCollab Team")
                        .email("dev@codecollab.io"))
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Placeholder bearer/JWT scheme.
     * Phase 1 will wire real token issuance; this makes the UI ready without code changes.
     */
    private SecurityScheme buildBearerScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("bearerAuth");
    }
}
