package com.codecollab.tag.service.impl;

import com.codecollab.tag.dto.TagResponse;
import com.codecollab.tag.dto.TagSuggestionResponse;
import com.codecollab.tag.entity.Tag;
import com.codecollab.tag.mapper.TagMapper;
import com.codecollab.tag.repository.TagRepository;
import com.codecollab.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TagServiceImpl implements TagService {

    private static final int MAX_TAGS_PER_SNIPPET = 10;

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> listTopTags(int limit) {
        return tagRepository.findTopTags(PageRequest.of(0, limit))
                .stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagSuggestionResponse> suggestTags(String prefix, int limit) {
        if (prefix == null || prefix.isBlank()) {
            return List.of();
        }
        String normalized = Tag.normalize(prefix);
        if (normalized == null || normalized.isEmpty()) {
            return List.of();
        }
        return tagRepository.searchByPrefix(normalized, PageRequest.of(0, limit))
                .stream()
                .map(tagMapper::toSuggestion)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Tag> resolveOrCreateTags(Set<String> rawTagNames) {
        if (rawTagNames == null || rawTagNames.isEmpty()) {
            return Set.of();
        }

        // Normalize all raw names, filter empties, deduplicate
        Set<String> normalized = rawTagNames.stream()
                .filter(n -> n != null && !n.isBlank())
                .map(Tag::normalize)
                .filter(n -> n != null && !n.isEmpty())
                .collect(Collectors.toSet());

        // Cap at MAX_TAGS_PER_SNIPPET — silently trim to first N
        if (normalized.size() > MAX_TAGS_PER_SNIPPET) {
            log.warn("Tag count {} exceeds max {}; trimming", normalized.size(), MAX_TAGS_PER_SNIPPET);
            normalized = normalized.stream()
                    .limit(MAX_TAGS_PER_SNIPPET)
                    .collect(Collectors.toSet());
        }

        Set<Tag> resolved = new HashSet<>();
        for (String name : normalized) {
            Tag tag = tagRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder()
                                .name(name)
                                .usageCount(0)
                                .build();
                        Tag saved = tagRepository.save(newTag);
                        log.debug("Created new tag: {}", saved.getName());
                        return saved;
                    });
            resolved.add(tag);
        }

        return resolved;
    }

    @Override
    public void incrementTagUsage(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        for (Long tagId : tagIds) {
            tagRepository.incrementUsage(tagId);
        }
    }

    @Override
    public void decrementTagsForSnippet(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            tagRepository.decrementUsage(tagId);
        }
    }
}
