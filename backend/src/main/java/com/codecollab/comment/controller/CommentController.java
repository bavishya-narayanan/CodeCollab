package com.codecollab.comment.controller;

import com.codecollab.comment.dto.CommentResponse;
import com.codecollab.comment.dto.CreateCommentRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/snippets/{snippetId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a comment or reply")
    public ApiResponse<CommentResponse> create(
            @PathVariable Long snippetId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateCommentRequest req) {
        return ApiResponse.ok("Comment created",
            commentService.create(snippetId, principal.getUserId(), req));
    }

    @GetMapping
    @Operation(summary = "List top-level comments for a snippet")
    public ApiResponse<PageResponse<CommentResponse>> list(
            @PathVariable Long snippetId,
            @PageableDefault(size = 20, sort = "createdAt", 
                             direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(
            commentService.listTopLevel(snippetId, pageable)));
    }
}
