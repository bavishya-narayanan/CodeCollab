package com.codecollab.ast.rule.impl;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.Severity;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DeepNestingRule implements AstRule {

    private static final int MAX_DEPTH = 4;

    @Override public String id()   { return "R005"; }
    @Override public String name() { return "DeepNesting"; }
    @Override public Severity defaultSeverity() { return Severity.WARNING; }

    @Override
    public List<AstIssue> analyze(AstRuleContext ctx) {
        List<AstIssue> issues = new ArrayList<>();
        
        for (MethodDeclaration m : ctx.allMethods()) {
            if (m.getBody().isPresent()) {
                int maxDepth = computeMaxNesting(m.getBody().get(), 0);
                if (maxDepth > MAX_DEPTH) {
                    issues.add(new AstIssue(
                        id(), name(), defaultSeverity(),
                        String.format("Method '%s' has a nesting depth of %d (max %d)",
                                      m.getNameAsString(), maxDepth, MAX_DEPTH),
                        m.getBegin().map(p -> p.line).orElse(0),
                        m.getBegin().map(p -> p.column).orElse(0),
                        Map.of("methodName", m.getNameAsString(),
                               "maxDepth", maxDepth,
                               "maxAllowed", MAX_DEPTH)
                    ));
                }
            }
        }
        
        return issues;
    }

    private int computeMaxNesting(Node node, int currentDepth) {
        int nextDepth = currentDepth;
        if (isNestingNode(node)) {
            nextDepth++;
        }
        
        int max = nextDepth;
        for (Node child : node.getChildNodes()) {
            // Do not recurse into anonymous classes or local classes defined inside a method
            if (child instanceof com.github.javaparser.ast.body.TypeDeclaration ||
                child instanceof com.github.javaparser.ast.expr.ObjectCreationExpr) {
                continue;
            }
            max = Math.max(max, computeMaxNesting(child, nextDepth));
        }
        return max;
    }

    private boolean isNestingNode(Node node) {
        return node instanceof IfStmt ||
               node instanceof ForStmt ||
               node instanceof ForEachStmt ||
               node instanceof WhileStmt ||
               node instanceof DoStmt ||
               node instanceof SwitchStmt ||
               node instanceof TryStmt;
    }
}
