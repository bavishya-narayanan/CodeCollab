package com.codecollab.vote.controller;

import com.codecollab.common.ApiResponse;
import com.codecollab.security.CustomUserDetails;
import com.codecollab.vote.dto.SnippetScoreResponse;
import com.codecollab.vote.dto.VoteRequest;
import com.codecollab.vote.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/snippets/{snippetId}/vote")
@RequiredArgsConstructor
@Tag(name = "Votes")
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    @Operation(summary = "Cast or change vote")
    public ApiResponse<SnippetScoreResponse> vote(
            @PathVariable Long snippetId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody VoteRequest req) {
        return ApiResponse.ok("Vote recorded",
            voteService.vote(snippetId, principal.getUserId(), req));
    }

    @DeleteMapping
    @Operation(summary = "Remove vote")
    public ApiResponse<SnippetScoreResponse> remove(
            @PathVariable Long snippetId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok("Vote removed",
            voteService.removeVote(snippetId, principal.getUserId()));
    }

    @GetMapping
    @Operation(summary = "Get score for a snippet")
    public ApiResponse<SnippetScoreResponse> score(
            @PathVariable Long snippetId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Long uid = (principal != null) ? principal.getUserId() : null;
        return ApiResponse.ok(voteService.getScore(snippetId, uid));
    }
}
