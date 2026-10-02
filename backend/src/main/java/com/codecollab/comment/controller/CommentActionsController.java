package com.codecollab.comment.controller;

import com.codecollab.comment.dto.CommentResponse;
import com.codecollab.comment.dto.UpdateCommentRequest;
import com.codecollab.comment.service.CommentService;
import com.codecollab.common.ApiResponse;
import com.codecollab.common.PageResponse;
import com.codecollab.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Actions")
public class CommentActionsController {

    private final CommentService commentService;

    @PutMapping("/{id}")
    @Operation(summary = "Edit own comment (within 15 min)")
    public ApiResponse<CommentResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateCommentRequest req) {
        return ApiResponse.ok("Comment updated",
            commentService.update(id, principal.getUserId(), req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete own comment")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        commentService.delete(id, principal.getUserId());
        return ApiResponse.ok("Comment deleted", null);
    }

    @PostMapping("/{id}/helpful")
    @Operation(summary = "Toggle helpful flag (snippet owner only)")
    public ApiResponse<CommentResponse> toggleHelpful(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok("Helpful toggled",
            commentService.toggleHelpful(id, principal.getUserId()));
    }

    @GetMapping("/{id}/replies")
    @Operation(summary = "List replies to a comment")
    public ApiResponse<PageResponse<CommentResponse>> replies(
            @PathVariable Long id,
            @PageableDefault(size = 20, sort = "createdAt", 
                             direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(
            commentService.listReplies(id, pageable)));
    }
}
