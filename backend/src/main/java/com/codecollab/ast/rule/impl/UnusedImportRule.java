package com.codecollab.ast.rule.impl;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.Severity;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UnusedImportRule implements AstRule {

    @Override public String id()   { return "R007"; }
    @Override public String name() { return "UnusedImport"; }
    @Override public Severity defaultSeverity() { return Severity.INFO; }

    @Override
    public List<AstIssue> analyze(AstRuleContext ctx) {
        List<AstIssue> issues = new ArrayList<>();
        
        List<NameExpr> allNameExprs = ctx.compilationUnit().findAll(NameExpr.class);
        
        ctx.compilationUnit().findAll(ImportDeclaration.class).forEach(imp -> {
            if (!imp.isAsterisk()) {
                String importName = imp.getNameAsString();
                String simpleName = importName.substring(importName.lastIndexOf('.') + 1);
                
                long uses = allNameExprs.stream()
                    .filter(n -> n.getNameAsString().equals(simpleName))
                    .count();
                    
                if (uses == 0) {
                    issues.add(new AstIssue(
                        id(), name(), defaultSeverity(),
                        "Unused import: " + importName,
                        imp.getBegin().map(p -> p.line).orElse(0),
                        imp.getBegin().map(p -> p.column).orElse(0),
                        Map.of("importName", importName,
                               "simpleName", simpleName)
                    ));
                }
            }
        });
        
        return issues;
    }
}
