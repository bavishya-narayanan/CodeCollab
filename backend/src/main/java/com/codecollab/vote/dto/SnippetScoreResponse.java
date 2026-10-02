package com.codecollab.vote.dto;

public record SnippetScoreResponse(
    Long snippetId,
    long upvotes,
    long downvotes,
    long score,
    Integer myVote
) {}
