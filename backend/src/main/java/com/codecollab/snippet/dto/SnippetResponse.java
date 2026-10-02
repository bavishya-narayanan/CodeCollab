package com.codecollab.snippet.dto;

import com.codecollab.tag.dto.TagResponse;

import java.time.Instant;
import java.util.List;

public record SnippetResponse(
    Long id,
    Long ownerId,
    String ownerUsername,
    String title,
    String description,
    String code,
    String language,
    Integer viewCount,
    List<TagResponse> tags,
    long upvoteCount,
    long downvoteCount,
    long voteScore,
    Integer myVote,
    long commentCount,
    Instant createdAt,
    Instant updatedAt
) {}
