package com.codecollab.ast.rule.impl;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.Severity;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MagicNumberRule implements AstRule {

    @Override public String id()   { return "R004"; }
    @Override public String name() { return "MagicNumber"; }
    @Override public Severity defaultSeverity() { return Severity.INFO; }

    @Override
    public List<AstIssue> analyze(AstRuleContext ctx) {
        List<AstIssue> issues = new ArrayList<>();
        
        ctx.compilationUnit().findAll(LiteralStringValueExpr.class).forEach(literal -> {
            if (literal instanceof IntegerLiteralExpr || literal instanceof DoubleLiteralExpr) {
                String value = literal.getValue();
                if (!isExemptValue(value) && !isExemptContext(literal)) {
                    issues.add(new AstIssue(
                        id(), name(), defaultSeverity(),
                        "Magic number detected: " + value,
                        literal.getBegin().map(p -> p.line).orElse(0),
                        literal.getBegin().map(p -> p.column).orElse(0),
                        Map.of("value", value)
                    ));
                }
            }
        });
        
        return issues;
    }

    private boolean isExemptValue(String value) {
        return value.equals("0") || value.equals("1") || value.equals("-1") ||
               value.equals("0.0") || value.equals("1.0") || value.equals("-1.0");
    }

    private boolean isExemptContext(Node node) {
        Node current = node.getParentNode().orElse(null);
        while (current != null) {
            if (current instanceof AnnotationExpr) {
                return true;
            }
            if (current instanceof EnumConstantDeclaration) {
                return true;
            }
            if (current instanceof FieldDeclaration fd) {
                if (fd.isStatic() && fd.isFinal()) {
                    return true;
                }
            }
            current = current.getParentNode().orElse(null);
        }
        return false;
    }
}
