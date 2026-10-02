package com.codecollab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for CodeCollab.
 *
 * <p>{@code @EnableJpaAuditing} is intentionally placed in its own {@code @Configuration}
 * class rather than on {@link com.codecollab.CodeCollabApplication}. This prevents
 * {@code @WebMvcTest} slices from failing with "JPA metamodel must not be empty" —
 * a well-known Spring Boot issue where JPA auditing beans are eagerly initialised
 * even when no JPA context is loaded in a web-layer test.
 *
 * <p>All future entities should extend a {@code BaseEntity} with
 * {@code @CreatedDate} / {@code @LastModifiedDate} fields to benefit from auditing.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // No beans needed — @EnableJpaAuditing does all the work via its BeanDefinitionRegistrar.
}
