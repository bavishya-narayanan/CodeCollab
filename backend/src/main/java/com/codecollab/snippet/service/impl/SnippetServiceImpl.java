package com.codecollab.snippet.service.impl;

import com.codecollab.snippet.dto.CreateSnippetRequest;
import com.codecollab.snippet.dto.SnippetResponse;
import com.codecollab.snippet.dto.SnippetSummaryResponse;
import com.codecollab.snippet.dto.UpdateSnippetRequest;
import com.codecollab.snippet.entity.Snippet;
import com.codecollab.snippet.exception.SnippetAccessDeniedException;
import com.codecollab.snippet.exception.SnippetNotFoundException;
import com.codecollab.snippet.mapper.SnippetMapper;
import com.codecollab.snippet.repository.SnippetRepository;
import com.codecollab.snippet.service.SnippetService;
import com.codecollab.tag.entity.Tag;
import com.codecollab.tag.service.TagService;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.repository.UserRepository;
import com.codecollab.ast.repository.AstResultRepository;
import com.codecollab.ast.service.AstAnalysisService;
import com.codecollab.vote.repository.VoteRepository;
import com.codecollab.comment.repository.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SnippetServiceImpl implements SnippetService {

    private final SnippetRepository snippetRepository;
    private final UserRepository userRepository;
    private final SnippetMapper snippetMapper;
    private final TagService tagService;
    private final AstAnalysisService astAnalysisService;
    private final AstResultRepository astResultRepository;
    private final StringRedisTemplate redis;
    private final VoteRepository voteRepository;
    private final CommentRepository commentRepository;

    @Override
    public SnippetResponse create(Long ownerId, CreateSnippetRequest request) {
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new EntityNotFoundException("Owner not found"));

        if (owner.getStatus() == UserStatus.BANNED) {
            throw new IllegalStateException("Banned users cannot create snippets");
        }

        Snippet snippet = snippetMapper.fromCreate(request, owner);
        snippet = snippetRepository.save(snippet);

        // Attach tags and increment their usage counts
        if (request.tags() != null && !request.tags().isEmpty()) {
            Set<Tag> resolved = tagService.resolveOrCreateTags(request.tags());
            resolved.forEach(snippet::addTag);
            snippet = snippetRepository.save(snippet);

            Set<Long> newTagIds = resolved.stream().map(Tag::getId).collect(Collectors.toSet());
            tagService.incrementTagUsage(newTagIds);
        }

        // Start AST analysis asynchronously
        astAnalysisService.analyzeAsync(snippet.getId(), snippet.getCode(), snippet.getLanguage().name());

        log.info("snippet {} created by user {}", snippet.getId(), owner.getUsername());
        return snippetMapper.toResponse(snippet, ownerId);
    }

    @Override
    public SnippetResponse getById(Long id, Long viewerId) {
        Snippet snippet = snippetRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new SnippetNotFoundException(id));

        if (!snippet.isOwnedBy(viewerId)) {
            snippetRepository.incrementViewCount(id);
            // Reload to get updated view count
            snippet = snippetRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new SnippetNotFoundException(id));
        }

        return snippetMapper.toResponse(snippet, viewerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnippetSummaryResponse> listAll(Pageable pageable) {
        Page<Snippet> page = snippetRepository.findAllByDeletedFalse(pageable);
        return mapToSummaries(page);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SnippetSummaryResponse> listByOwner(Long ownerId, Pageable pageable) {
        if (!userRepository.existsById(ownerId)) {
            throw new EntityNotFoundException("Owner not found");
        }
        Page<Snippet> page = snippetRepository.findAllByOwnerIdAndDeletedFalse(ownerId, pageable);
        return mapToSummaries(page);
    }

    private Page<SnippetSummaryResponse> mapToSummaries(Page<Snippet> page) {
        if (page.isEmpty()) {
            return page.map(s -> snippetMapper.toSummary(s, 0, 0));
        }
        
        List<Long> ids = page.stream().map(Snippet::getId).toList();
        
        Map<Long, Long> scores = voteRepository.scoresBySnippetIds(ids).stream()
            .collect(Collectors.toMap(VoteRepository.SnippetScore::getSnippetId, VoteRepository.SnippetScore::getScore));
            
        Map<Long, Long> commentCounts = commentRepository.countBySnippetIds(ids).stream()
            .collect(Collectors.toMap(CommentRepository.SnippetCommentCount::getSnippetId, CommentRepository.SnippetCommentCount::getCnt));
            
        return page.map(s -> {
            long score = scores.getOrDefault(s.getId(), 0L);
            long commentCount = commentCounts.getOrDefault(s.getId(), 0L);
            return snippetMapper.toSummary(s, score, commentCount);
        });
    }

    @Override
    public SnippetResponse update(Long snippetId, Long actorId, UpdateSnippetRequest request) {
        Snippet snippet = snippetRepository.findByIdAndDeletedFalse(snippetId)
            .orElseThrow(() -> new SnippetNotFoundException(snippetId));

        if (!snippet.isOwnedBy(actorId)) {
            throw new SnippetAccessDeniedException();
        }

        snippetMapper.applyUpdate(snippet, request);

        // Handle tag updates only when tags field is explicitly provided
        if (request.tags() != null) {
            Set<Tag> oldTags = new HashSet<>(snippet.getTags());
            Set<Long> oldTagIds = oldTags.stream().map(Tag::getId).collect(Collectors.toSet());

            snippet.clearTags();

            if (!request.tags().isEmpty()) {
                Set<Tag> newTags = tagService.resolveOrCreateTags(request.tags());
                Set<Long> newTagIds = newTags.stream().map(Tag::getId).collect(Collectors.toSet());

                newTags.forEach(snippet::addTag);

                // Decrement usage for removed tags (old - new)
                Set<Long> removedIds = new HashSet<>(oldTagIds);
                removedIds.removeAll(newTagIds);
                tagService.decrementTagsForSnippet(removedIds);

                // Increment usage for added tags (new - old)
                Set<Long> addedIds = new HashSet<>(newTagIds);
                addedIds.removeAll(oldTagIds);
                tagService.incrementTagUsage(addedIds);
            } else {
                // Tags explicitly cleared — decrement all old ones
                tagService.decrementTagsForSnippet(oldTagIds);
            }
        }

        snippet = snippetRepository.save(snippet);

        // Start AST analysis asynchronously and clear cache
        redis.delete("ast:cache:" + snippet.getId());
        astAnalysisService.analyzeAsync(snippet.getId(), snippet.getCode(), snippet.getLanguage().name());

        log.info("snippet {} updated by user {}", snippet.getId(), actorId);
        return snippetMapper.toResponse(snippet, actorId);
    }

    @Override
    public void delete(Long snippetId, Long actorId) {
        Snippet snippet = snippetRepository.findByIdAndDeletedFalse(snippetId)
            .orElseThrow(() -> new SnippetNotFoundException(snippetId));

        if (!snippet.isOwnedBy(actorId)) {
            throw new SnippetAccessDeniedException();
        }

        // Collect tag IDs before soft-deleting
        Set<Long> tagIds = snippet.getTags().stream()
                .map(Tag::getId)
                .collect(Collectors.toSet());

        snippet.softDelete();
        snippetRepository.save(snippet);

        // Decrement usage counts for all tags this snippet was using
        tagService.decrementTagsForSnippet(tagIds);

        // Delete AST results
        astResultRepository.deleteBySnippetId(snippetId);
        redis.delete("ast:cache:" + snippetId);

        log.info("snippet {} soft-deleted by user {}", snippet.getId(), actorId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOwner(Long ownerId) {
        return snippetRepository.countByOwnerIdAndDeletedFalse(ownerId);
    }
}
