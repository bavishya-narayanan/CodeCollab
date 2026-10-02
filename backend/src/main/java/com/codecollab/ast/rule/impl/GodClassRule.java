package com.codecollab.ast.rule.impl;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.Severity;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GodClassRule implements AstRule {

    private static final int MAX_METHODS = 15;

    @Override public String id()   { return "R006"; }
    @Override public String name() { return "GodClass"; }
    @Override public Severity defaultSeverity() { return Severity.WARNING; }

    @Override
    public List<AstIssue> analyze(AstRuleContext ctx) {
        List<AstIssue> issues = new ArrayList<>();
        
        ctx.compilationUnit().findAll(ClassOrInterfaceDeclaration.class).forEach(cid -> {
            if (!cid.isInterface()) {
                long methodCount = cid.getMethods().stream()
                    .filter(m -> m.getParentNode().map(p -> p == cid).orElse(false))
                    .count();
                    
                if (methodCount > MAX_METHODS) {
                    issues.add(new AstIssue(
                        id(), name(), defaultSeverity(),
                        String.format("Class '%s' has %d methods (max %d)",
                                      cid.getNameAsString(), methodCount, MAX_METHODS),
                        cid.getBegin().map(p -> p.line).orElse(0),
                        cid.getBegin().map(p -> p.column).orElse(0),
                        Map.of("className", cid.getNameAsString(),
                               "methodCount", (int) methodCount,
                               "maxMethods", MAX_METHODS)
                    ));
                }
            }
        });
        
        return issues;
    }
}
