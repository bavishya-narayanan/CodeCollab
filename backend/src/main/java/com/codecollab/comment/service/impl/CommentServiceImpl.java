package com.codecollab.comment.service.impl;

import com.codecollab.comment.dto.CommentResponse;
import com.codecollab.comment.dto.CreateCommentRequest;
import com.codecollab.comment.dto.UpdateCommentRequest;
import com.codecollab.comment.entity.Comment;
import com.codecollab.comment.exception.CommentDepthExceededException;
import com.codecollab.comment.exception.CommentEditWindowExpiredException;
import com.codecollab.comment.exception.CommentNotFoundException;
import com.codecollab.comment.mapper.CommentMapper;
import com.codecollab.comment.repository.CommentRepository;
import com.codecollab.comment.service.CommentService;
import com.codecollab.snippet.entity.Snippet;
import com.codecollab.snippet.exception.SnippetAccessDeniedException;
import com.codecollab.snippet.exception.SnippetNotFoundException;
import com.codecollab.snippet.repository.SnippetRepository;
import com.codecollab.user.entity.User;
import com.codecollab.user.entity.UserStatus;
import com.codecollab.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommentServiceImpl implements CommentService {

    private static final Duration EDIT_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_DEPTH = 1;

    private final CommentRepository commentRepository;
    private final SnippetRepository snippetRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentResponse create(Long snippetId, Long authorId, CreateCommentRequest req) {
        if (!snippetRepository.existsByIdAndDeletedFalse(snippetId)) {
            throw new SnippetNotFoundException(snippetId);
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author not found"));
        if (author.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only active users can comment");
        }

        if (req.parentCommentId() != null) {
            Comment parent = commentRepository.findByIdAndDeletedFalse(req.parentCommentId())
                    .orElseThrow(() -> new CommentNotFoundException(req.parentCommentId()));
            if (parent.getParentId() != null) {
                throw new CommentDepthExceededException();
            }
            if (!parent.getSnippetId().equals(snippetId)) {
                throw new IllegalArgumentException("Parent comment belongs to a different snippet");
            }
        }

        Comment comment = Comment.builder()
                .snippetId(snippetId)
                .userId(authorId)
                .parentId(req.parentCommentId())
                .content(req.content())
                .build();

        comment = commentRepository.save(comment);
        return commentMapper.toResponse(comment, 0);
    }

    @Override
    public CommentResponse update(Long commentId, Long actorId, UpdateCommentRequest req) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.isOwnedBy(actorId)) {
            throw new SnippetAccessDeniedException(); // Reusing the same 403 exception
        }

        if (comment.getCreatedAt().plus(EDIT_WINDOW).isBefore(Instant.now())) {
            throw new CommentEditWindowExpiredException();
        }

        comment.setContent(req.content());
        comment = commentRepository.save(comment);
        long replyCount = commentRepository.countByParentIdAndDeletedFalse(commentId);
        return commentMapper.toResponse(comment, replyCount);
    }

    @Override
    public void delete(Long commentId, Long actorId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        if (!comment.isOwnedBy(actorId)) {
            throw new SnippetAccessDeniedException();
        }

        comment.setDeleted(true);
        comment.setContent("[deleted]");
        commentRepository.save(comment);
    }

    @Override
    public CommentResponse toggleHelpful(Long commentId, Long actorId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        Long snippetId = comment.getSnippetId();
        Snippet snippet = snippetRepository.findByIdAndDeletedFalse(snippetId)
                .orElseThrow(() -> new SnippetNotFoundException(snippetId));

        if (!snippet.isOwnedBy(actorId)) {
            throw new SnippetAccessDeniedException();
        }

        comment.setHelpful(!comment.isHelpful());
        comment = commentRepository.save(comment);
        long replyCount = commentRepository.countByParentIdAndDeletedFalse(commentId);
        return commentMapper.toResponse(comment, replyCount);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> listTopLevel(Long snippetId, Pageable pageable) {
        return commentRepository.findAllBySnippetIdAndParentIdIsNullAndDeletedFalse(snippetId, pageable)
                .map(c -> {
                    long rc = commentRepository.countByParentIdAndDeletedFalse(c.getId());
                    return commentMapper.toResponse(c, rc);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> listReplies(Long parentCommentId, Pageable pageable) {
        return commentRepository.findAllByParentIdAndDeletedFalse(parentCommentId, pageable)
                .map(c -> commentMapper.toResponse(c, 0));
    }

    @Override
    @Transactional(readOnly = true)
    public long countBySnippet(Long snippetId) {
        return commentRepository.countBySnippetIdAndDeletedFalse(snippetId);
    }
}
