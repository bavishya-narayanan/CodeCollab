package com.codecollab.tag.controller;

import com.codecollab.common.ApiResponse;
import com.codecollab.tag.dto.TagResponse;
import com.codecollab.tag.dto.TagSuggestionResponse;
import com.codecollab.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tags", description = "Tag listing and autocomplete")
public class TagController {

    private final TagService tagService;

    @GetMapping
    @Operation(summary = "List top tags by usage count")
    public ApiResponse<List<TagResponse>> listTop(
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit) {
        return ApiResponse.ok(tagService.listTopTags(limit));
    }

    @GetMapping("/suggest")
    @Operation(summary = "Autocomplete tag suggestions by prefix")
    public ApiResponse<List<TagSuggestionResponse>> suggest(
            @RequestParam @NotBlank String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return ApiResponse.ok(tagService.suggestTags(q, limit));
    }
}
