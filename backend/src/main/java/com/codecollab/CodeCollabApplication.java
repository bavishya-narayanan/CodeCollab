package com.codecollab;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Main entry point for the CodeCollab application.
 *
 * <p>Phase 0: Foundation only — no business logic, just infrastructure wiring.
 * {@code @EnableJpaAuditing} is declared here so all future entities automatically
 * inherit {@code createdAt} / {@code updatedAt} auditing without extra configuration.
 */
@SpringBootApplication
@Slf4j
public class CodeCollabApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CodeCollabApplication.class, args);

        // Resolve the actual bound port — useful when port is randomised in tests
        String port = context.getEnvironment().getProperty("server.port", "8080");
        log.info("🚀 CodeCollab backend started on port {}", port);
    }
}
