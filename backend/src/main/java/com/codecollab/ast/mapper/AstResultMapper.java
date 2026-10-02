package com.codecollab.ast.mapper;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.dto.AstMetrics;
import com.codecollab.ast.dto.AstResultResponse;
import com.codecollab.ast.entity.AstResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AstResultMapper {

    private final ObjectMapper objectMapper;

    public AstResultResponse toResponse(AstResult entity) {
        List<AstIssue> issues = List.of();
        AstMetrics metrics = null;
        
        try {
            if (entity.getIssues() != null && !entity.getIssues().isBlank()) {
                issues = objectMapper.readValue(entity.getIssues(), new TypeReference<List<AstIssue>>() {});
            }
            if (entity.getMetrics() != null && !entity.getMetrics().isBlank()) {
                metrics = objectMapper.readValue(entity.getMetrics(), AstMetrics.class);
            }
        } catch (Exception e) {
            // Log warning in real app
        }
        
        return new AstResultResponse(
            entity.getSnippetId(),
            entity.getStatus(),
            entity.getLanguage(),
            issues,
            metrics,
            entity.getQualityScore(),
            entity.getErrorMessage(),
            entity.getAnalyzedAt(),
            entity.getDurationMs(),
            entity.getRuleVersion()
        );
    }
}
