package com.codecollab.ast.dto;

public record AstMetrics(
    int totalLines,
    int codeLines,
    int commentLines,
    int blankLines,
    int classCount,
    int methodCount,
    int averageMethodLength,
    int maxMethodLength,
    int maxNestingDepth,
    int cyclomaticComplexity,
    int importCount,
    int fieldCount
) {}
