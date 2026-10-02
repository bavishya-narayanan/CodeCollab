package com.codecollab.snippet.mapper;

import com.codecollab.snippet.dto.CreateSnippetRequest;
import com.codecollab.snippet.dto.SnippetResponse;
import com.codecollab.snippet.dto.SnippetSummaryResponse;
import com.codecollab.snippet.dto.UpdateSnippetRequest;
import com.codecollab.snippet.entity.Snippet;
import com.codecollab.snippet.entity.SnippetLanguage;
import com.codecollab.tag.dto.TagResponse;
import com.codecollab.tag.entity.Tag;
import com.codecollab.user.entity.User;
import com.codecollab.vote.repository.VoteRepository;
import com.codecollab.vote.entity.VoteType;
import com.codecollab.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SnippetMapper {

    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;

    public SnippetResponse toResponse(Snippet s) {
        return toResponse(s, null);
    }

    public SnippetResponse toResponse(Snippet s, Long viewerId) {
        long upvoteCount = voteRepository.countBySnippetIdAndVoteType(s.getId(), VoteType.UP);
        long downvoteCount = voteRepository.countBySnippetIdAndVoteType(s.getId(), VoteType.DOWN);
        long voteScore = upvoteCount - downvoteCount;
        Integer myVote = (viewerId == null) ? null : 
            voteRepository.findBySnippetIdAndUserId(s.getId(), viewerId)
                .map(v -> v.getVoteType().value())
                .orElse(null);
        long commentCount = commentRepository.countBySnippetIdAndDeletedFalse(s.getId());

        return new SnippetResponse(
            s.getId(),
            s.getOwner().getId(),
            s.getOwner().getUsername(),
            s.getTitle(),
            s.getDescription(),
            s.getCode(),
            s.getLanguage().name(),
            s.getViewCount(),
            toTagResponses(s.getTags()),
            upvoteCount,
            downvoteCount,
            voteScore,
            myVote,
            commentCount,
            s.getCreatedAt(),
            s.getUpdatedAt()
        );
    }

    public SnippetSummaryResponse toSummary(Snippet s) {
        return toSummary(s, 0, 0);
    }

    public SnippetSummaryResponse toSummary(Snippet s, long score, long commentCount) {
        return new SnippetSummaryResponse(
            s.getId(),
            s.getOwner().getId(),
            s.getOwner().getUsername(),
            s.getTitle(),
            s.getDescription(),
            s.getLanguage().name(),
            s.getViewCount(),
            toTagNames(s.getTags()),
            score,
            commentCount,
            s.getCreatedAt(),
            s.getUpdatedAt()
        );
    }

    public Snippet fromCreate(CreateSnippetRequest req, User owner) {
        return Snippet.builder()
            .owner(owner)
            .title(req.title().trim())
            .description(req.description() != null ? req.description().trim() : null)
            .code(req.code())
            .language(SnippetLanguage.fromString(req.language()))
            .viewCount(0)
            .deleted(false)
            .build();
    }

    public void applyUpdate(Snippet snippet, UpdateSnippetRequest req) {
        if (req.title() != null && !req.title().isBlank()) {
            snippet.setTitle(req.title().trim());
        }
        if (req.description() != null) {
            snippet.setDescription(req.description().trim());
        }
        if (req.code() != null && !req.code().isBlank()) {
            snippet.setCode(req.code());
        }
        if (req.language() != null && !req.language().isBlank()) {
            snippet.setLanguage(SnippetLanguage.fromString(req.language()));
        }
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private List<TagResponse> toTagResponses(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) return Collections.emptyList();
        return tags.stream()
                .sorted(Comparator.comparing(Tag::getName))
                .map(t -> new TagResponse(t.getId(), t.getName(), t.getUsageCount()))
                .collect(Collectors.toList());
    }

    private List<String> toTagNames(Set<Tag> tags) {
        if (tags == null || tags.isEmpty()) return Collections.emptyList();
        return tags.stream()
                .map(Tag::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}
