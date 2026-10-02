package com.codecollab.snippet.controller;

import com.codecollab.common.ApiResponse;
import com.codecollab.common.PageResponse;
import com.codecollab.security.CustomUserDetails;
import com.codecollab.snippet.dto.CreateSnippetRequest;
import com.codecollab.snippet.dto.SnippetResponse;
import com.codecollab.snippet.dto.SnippetSummaryResponse;
import com.codecollab.snippet.dto.UpdateSnippetRequest;
import com.codecollab.snippet.service.SnippetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/snippets")
@RequiredArgsConstructor
@Tag(name = "Snippets", description = "Code snippet operations")
@PreAuthorize("isAuthenticated()")
public class SnippetController {

    private final SnippetService snippetService;

    @PostMapping
    @Operation(summary = "Create a new snippet")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SnippetResponse> create(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CreateSnippetRequest request) {
        SnippetResponse created = snippetService.create(principal.getUserId(), request);
        return ApiResponse.ok("Snippet created", created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a snippet by ID")
    public ApiResponse<SnippetResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ApiResponse.ok(snippetService.getById(id, principal.getUserId()));
    }

    @GetMapping
    @Operation(summary = "List snippets (paginated)")
    public ApiResponse<PageResponse<SnippetSummaryResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<SnippetSummaryResponse> page = snippetService.listAll(pageable);
        return ApiResponse.ok(PageResponse.from(page));
    }

    @GetMapping("/mine")
    @Operation(summary = "List my snippets (paginated)")
    public ApiResponse<PageResponse<SnippetSummaryResponse>> mine(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<SnippetSummaryResponse> page = snippetService.listByOwner(principal.getUserId(), pageable);
        return ApiResponse.ok(PageResponse.from(page));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update own snippet")
    public ApiResponse<SnippetResponse> update(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateSnippetRequest request) {
        return ApiResponse.ok("Snippet updated",
            snippetService.update(id, principal.getUserId(), request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete own snippet")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails principal) {
        snippetService.delete(id, principal.getUserId());
        return ApiResponse.ok("Snippet deleted", null);
    }
}
