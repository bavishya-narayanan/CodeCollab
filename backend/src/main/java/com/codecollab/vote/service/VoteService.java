package com.codecollab.vote.service;

import com.codecollab.vote.dto.SnippetScoreResponse;
import com.codecollab.vote.dto.VoteRequest;

import java.util.List;
import java.util.Map;

public interface VoteService {
    SnippetScoreResponse vote(Long snippetId, Long userId, VoteRequest req);
    SnippetScoreResponse removeVote(Long snippetId, Long userId);
    SnippetScoreResponse getScore(Long snippetId, Long viewerId);
    Map<Long, Integer> scoresFor(List<Long> snippetIds);
}
