package com.codecollab.tag.mapper;

import com.codecollab.tag.dto.TagResponse;
import com.codecollab.tag.dto.TagSuggestionResponse;
import com.codecollab.tag.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getUsageCount());
    }

    public TagSuggestionResponse toSuggestion(Tag tag) {
        return new TagSuggestionResponse(tag.getName(), tag.getUsageCount());
    }
}
