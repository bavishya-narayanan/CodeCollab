package com.codecollab.comment.service;

import com.codecollab.comment.dto.CommentResponse;
import com.codecollab.comment.dto.CreateCommentRequest;
import com.codecollab.comment.dto.UpdateCommentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse create(Long snippetId, Long authorId, CreateCommentRequest req);
    CommentResponse update(Long commentId, Long actorId, UpdateCommentRequest req);
    void delete(Long commentId, Long actorId);
    CommentResponse toggleHelpful(Long commentId, Long actorId);
    Page<CommentResponse> listTopLevel(Long snippetId, Pageable pageable);
    Page<CommentResponse> listReplies(Long parentCommentId, Pageable pageable);
    long countBySnippet(Long snippetId);
}
