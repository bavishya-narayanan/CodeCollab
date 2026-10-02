package com.codecollab.ast.dto;

import java.time.Instant;
import java.util.List;

public record AstResultResponse(
    Long snippetId,
    AnalysisStatus status,
    String language,
    List<AstIssue> issues,
    AstMetrics metrics,
    Integer qualityScore,
    String errorMessage,
    Instant analyzedAt,
    Integer durationMs,
    String ruleVersion
) {}
