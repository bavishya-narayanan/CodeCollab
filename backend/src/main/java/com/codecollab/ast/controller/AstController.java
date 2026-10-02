package com.codecollab.ast.controller;

import com.codecollab.ast.dto.AstResultResponse;
import com.codecollab.ast.service.AstAnalysisService;
import com.codecollab.common.ApiResponse;
import com.codecollab.security.CustomUserDetails;
import com.codecollab.snippet.repository.SnippetRepository;
import jakarta.persistence.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/snippets/{snippetId}/analysis")
@RequiredArgsConstructor
@Tag(name = "Code Analysis", description = "Automatic code quality analysis")
public class AstController {

    private final AstAnalysisService analysisService;
    private final SnippetRepository snippetRepository;

    @GetMapping
    @Operation(summary = "Get analysis for a snippet")
    public ApiResponse<AstResultResponse> get(@PathVariable Long snippetId) {
        if (snippetRepository.findByIdAndDeletedFalse(snippetId).isEmpty()) {
            throw new EntityNotFoundException("Snippet not found");
        }
        return ApiResponse.ok(analysisService.getBySnippetId(snippetId));
    }

    @PostMapping("/reanalyze")
    @Operation(summary = "Force reanalysis of a snippet")
    public ApiResponse<AstResultResponse> reanalyze(
            @PathVariable Long snippetId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok("Reanalysis successful", analysisService.reanalyze(snippetId, principal.getUserId()));
    }
}
