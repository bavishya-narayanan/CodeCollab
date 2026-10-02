package com.codecollab.ast.dto;

import com.codecollab.ast.rule.Severity;
import java.util.Map;

public record AstIssue(
    String ruleId,
    String ruleName,
    Severity severity,
    String message,
    int line,
    int column,
    Map<String, Object> metadata
) {}
