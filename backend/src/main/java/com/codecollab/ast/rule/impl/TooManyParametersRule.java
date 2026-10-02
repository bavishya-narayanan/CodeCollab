package com.codecollab.ast.rule.impl;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.Severity;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TooManyParametersRule implements AstRule {

    private static final int MAX_PARAMS = 5;

    @Override public String id()   { return "R002"; }
    @Override public String name() { return "TooManyParameters"; }
    @Override public Severity defaultSeverity() { return Severity.WARNING; }

    @Override
    public List<AstIssue> analyze(AstRuleContext ctx) {
        List<AstIssue> issues = new ArrayList<>();
        for (MethodDeclaration m : ctx.allMethods()) {
            int paramCount = m.getParameters().size();
            if (paramCount > MAX_PARAMS) {
                issues.add(new AstIssue(
                    id(), name(), defaultSeverity(),
                    String.format("Method '%s' has %d parameters (max %d)",
                                  m.getNameAsString(), paramCount, MAX_PARAMS),
                    m.getBegin().map(p -> p.line).orElse(0),
                    m.getBegin().map(p -> p.column).orElse(0),
                    Map.of("methodName", m.getNameAsString(),
                           "paramCount", paramCount,
                           "maxParams", MAX_PARAMS)
                ));
            }
        }
        return issues;
    }
}
