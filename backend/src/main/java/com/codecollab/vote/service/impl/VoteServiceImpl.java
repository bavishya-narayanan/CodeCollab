package com.codecollab.vote.service.impl;

import com.codecollab.snippet.entity.Snippet;
import com.codecollab.snippet.exception.SnippetNotFoundException;
import com.codecollab.snippet.repository.SnippetRepository;
import com.codecollab.vote.dto.SnippetScoreResponse;
import com.codecollab.vote.dto.VoteRequest;
import com.codecollab.vote.entity.Vote;
import com.codecollab.vote.entity.VoteType;
import com.codecollab.vote.exception.CannotVoteOwnSnippetException;
import com.codecollab.vote.repository.VoteRepository;
import com.codecollab.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VoteServiceImpl implements VoteService {

    private final VoteRepository voteRepository;
    private final SnippetRepository snippetRepository;

    @Override
    public SnippetScoreResponse vote(Long snippetId, Long userId, VoteRequest req) {
        Snippet snippet = snippetRepository.findByIdAndDeletedFalse(snippetId)
            .orElseThrow(() -> new SnippetNotFoundException(snippetId));

        if (snippet.isOwnedBy(userId)) {
            throw new CannotVoteOwnSnippetException();
        }

        VoteType target = VoteType.fromValue(req.vote());
        Optional<Vote> existing = voteRepository.findBySnippetIdAndUserId(snippetId, userId);

        if (existing.isPresent()) {
            Vote v = existing.get();
            if (v.getVoteType() == target) {
                return buildResponse(snippetId, userId);
            }
            v.setVoteType(target);
            voteRepository.save(v);
        } else {
            voteRepository.save(Vote.builder()
                .snippetId(snippetId)
                .userId(userId)
                .voteType(target)
                .build());
        }

        return buildResponse(snippetId, userId);
    }

    @Override
    public SnippetScoreResponse removeVote(Long snippetId, Long userId) {
        voteRepository.findBySnippetIdAndUserId(snippetId, userId)
            .ifPresent(voteRepository::delete);
        return buildResponse(snippetId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public SnippetScoreResponse getScore(Long snippetId, Long viewerId) {
        return buildResponse(snippetId, viewerId);
    }

    private SnippetScoreResponse buildResponse(Long snippetId, Long viewerId) {
        long up = voteRepository.countBySnippetIdAndVoteType(snippetId, VoteType.UP);
        long down = voteRepository.countBySnippetIdAndVoteType(snippetId, VoteType.DOWN);
        Integer myVote = (viewerId == null) ? null
            : voteRepository.findBySnippetIdAndUserId(snippetId, viewerId)
                .map(v -> v.getVoteType().value())
                .orElse(null);
        return new SnippetScoreResponse(snippetId, up, down, up - down, myVote);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> scoresFor(List<Long> snippetIds) {
        if (snippetIds.isEmpty()) return Map.of();
        Map<Long, Integer> out = new HashMap<>();
        for (VoteRepository.SnippetScore row : voteRepository.scoresBySnippetIds(snippetIds)) {
            out.put(row.getSnippetId(), row.getScore() == null ? 0 : row.getScore().intValue());
        }
        return out;
    }
}
