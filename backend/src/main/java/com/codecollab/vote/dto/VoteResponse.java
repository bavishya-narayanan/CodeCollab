package com.codecollab.vote.dto;

public record VoteResponse(
    Long snippetId,
    long upvotes,
    long downvotes,
    long score
) {}
