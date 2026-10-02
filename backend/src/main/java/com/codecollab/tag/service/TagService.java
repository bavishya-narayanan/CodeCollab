package com.codecollab.tag.service;

import com.codecollab.tag.dto.TagResponse;
import com.codecollab.tag.dto.TagSuggestionResponse;
import com.codecollab.tag.entity.Tag;

import java.util.List;
import java.util.Set;

public interface TagService {

    List<TagResponse> listTopTags(int limit);

    List<TagSuggestionResponse> suggestTags(String prefix, int limit);

    /**
     * Find existing tags or create new ones for a set of raw tag names.
     * Does NOT increment usage counts — caller decides when to increment.
     */
    Set<Tag> resolveOrCreateTags(Set<String> rawTagNames);

    /**
     * Increment usage counts for a set of tag IDs, typically after a
     * snippet is created or its tags are updated.
     */
    void incrementTagUsage(Set<Long> tagIds);

    /**
     * Decrement usage counts for a set of tag IDs, typically after a
     * snippet is soft-deleted or tags are removed.
     */
    void decrementTagsForSnippet(Set<Long> tagIds);
}
