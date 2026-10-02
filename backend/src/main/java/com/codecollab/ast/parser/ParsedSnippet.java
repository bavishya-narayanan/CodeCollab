package com.codecollab.ast.parser;

import com.github.javaparser.ast.CompilationUnit;

public record ParsedSnippet(
    CompilationUnit compilationUnit,
    String originalCode,
    boolean parsedSuccessfully,
    String errorMessage
) {
    public static ParsedSnippet success(CompilationUnit cu, String code) {
        return new ParsedSnippet(cu, code, true, null);
    }
    public static ParsedSnippet failure(String code, String error) {
        return new ParsedSnippet(null, code, false, error);
    }
}
