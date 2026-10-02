package com.codecollab.tag.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (usageCount == null) usageCount = 0;
        if (name != null) name = name.toLowerCase().trim();
    }

    /**
     * Normalize a raw tag string to a safe, consistent format.
     * Converts to lowercase, replaces non-alphanumeric characters
     * (except +, #, .) with hyphens, collapses consecutive hyphens,
     * and strips leading/trailing hyphens.
     */
    public static String normalize(String raw) {
        if (raw == null) return null;
        return raw.trim()
                  .toLowerCase()
                  .replaceAll("[^a-z0-9+#.-]", "-")
                  .replaceAll("-{2,}", "-")
                  .replaceAll("^-|-$", "");
    }
}
