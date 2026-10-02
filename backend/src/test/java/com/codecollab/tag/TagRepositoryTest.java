package com.codecollab.tag;

import com.codecollab.tag.entity.Tag;
import com.codecollab.tag.repository.TagRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TagRepositoryTest — requires Testcontainers with a running Docker daemon.
 * Disabled by default in environments where Docker/WSL2 is not available.
 * To enable: remove @Disabled and ensure Docker Desktop is running.
 */
@Disabled("Requires Testcontainers / Docker")
@DataJpaTest
class TagRepositoryTest {

    @Autowired
    TagRepository tagRepository;

    @Test
    void findByNameIgnoreCase_returnsMatchingTag() {
        Tag saved = tagRepository.save(Tag.builder().name("jwt").usageCount(5).build());

        assertThat(tagRepository.findByNameIgnoreCase("JWT")).isPresent()
                .hasValueSatisfying(t -> assertThat(t.getId()).isEqualTo(saved.getId()));
    }

    @Test
    void existsByNameIgnoreCase_works() {
        tagRepository.save(Tag.builder().name("spring-boot").usageCount(0).build());

        assertThat(tagRepository.existsByNameIgnoreCase("SPRING-BOOT")).isTrue();
        assertThat(tagRepository.existsByNameIgnoreCase("nosuchTag")).isFalse();
    }

    @Test
    void incrementUsage_incrementsCount() {
        Tag tag = tagRepository.save(Tag.builder().name("redis").usageCount(2).build());

        tagRepository.incrementUsage(tag.getId());
        tagRepository.flush();

        Tag updated = tagRepository.findById(tag.getId()).orElseThrow();
        assertThat(updated.getUsageCount()).isEqualTo(3);
    }

    @Test
    void searchByPrefix_returnsMatchesOrderedByUsage() {
        tagRepository.save(Tag.builder().name("java").usageCount(10).build());
        tagRepository.save(Tag.builder().name("javascript").usageCount(3).build());
        tagRepository.save(Tag.builder().name("jakarta").usageCount(7).build());

        List<Tag> results = tagRepository.searchByPrefix("ja", PageRequest.of(0, 10));

        assertThat(results).extracting(Tag::getName)
                .containsExactly("java", "jakarta", "javascript"); // ordered by usageCount DESC
    }
}
