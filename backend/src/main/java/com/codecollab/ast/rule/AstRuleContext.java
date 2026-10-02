package com.codecollab.ast.rule;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.List;

public record AstRuleContext(
    CompilationUnit compilationUnit,
    String originalCode,
    String language,
    int[] lineOffsets
) {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<TypeDeclaration<?>> allTypes() {
        return (List) compilationUnit.findAll(TypeDeclaration.class);
    }

    public List<MethodDeclaration> allMethods() {
        return compilationUnit.findAll(MethodDeclaration.class);
    }
}
