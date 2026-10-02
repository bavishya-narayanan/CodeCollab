package com.codecollab.snippet.controller;

import com.codecollab.common.ApiResponse;
import com.codecollab.common.PageResponse;
import com.codecollab.snippet.dto.SnippetSearchRequest;
import com.codecollab.snippet.dto.SnippetSummaryResponse;
import com.codecollab.snippet.service.SnippetSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/snippets/search")
@RequiredArgsConstructor
@Tag(name = "Snippet Search", description = "Full-text search + language/tag/owner filters")
@SecurityRequirement(name = "bearerAuth")
public class SnippetSearchController {

    private final SnippetSearchService searchService;

    /**
     * GET /api/snippets/search
     *
     * Spring MVC resolves exact literal path segments before path variables,
     * so this route takes priority over /api/snippets/{id} in SnippetController.
     */
    @GetMapping
    @Operation(summary = "Search snippets by text, language, tags, or owner")
    public ApiResponse<PageResponse<SnippetSummaryResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) List<String> tag,
            @PageableDefault(size = 20) Pageable pageable) {

        Set<String> tags = tag == null ? Set.of()
                : tag.stream()
                     .map(String::toLowerCase)
                     .collect(Collectors.toSet());

        SnippetSearchRequest request = new SnippetSearchRequest(q, language, ownerId, tags);

        return ApiResponse.ok(PageResponse.from(searchService.search(request, pageable)));
    }

    @GetMapping("/recent")
    @Operation(summary = "Get recently searched terms (from Redis)")
    public ApiResponse<List<String>> recent(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(searchService.getRecentSearches(limit));
    }
}
