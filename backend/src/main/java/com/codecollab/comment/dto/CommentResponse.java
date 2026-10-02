package com.codecollab.comment.dto;

import java.time.Instant;
import java.util.List;

public record CommentResponse(
    Long id,
    Long snippetId,
    Long userId,
    String authorUsername,
    Long parentId,
    String content,
    boolean helpful,
    boolean deleted,
    boolean edited,
    Instant createdAt,
    Instant updatedAt,
    long replyCount,
    List<CommentResponse> replies
) {}
