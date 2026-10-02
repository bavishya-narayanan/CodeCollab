package com.codecollab.ast.rule.impl;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.Severity;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LongMethodRule implements AstRule {

    private static final int MAX_LINES = 30;

    @Override public String id()   { return "R001"; }
    @Override public String name() { return "LongMethod"; }
    @Override public Severity defaultSeverity() { return Severity.WARNING; }

    @Override
    public List<AstIssue> analyze(AstRuleContext ctx) {
        List<AstIssue> issues = new ArrayList<>();
        for (MethodDeclaration m : ctx.allMethods()) {
            int lines = m.getEnd().map(e -> e.line).orElse(0)
                      - m.getBegin().map(b -> b.line).orElse(0) + 1;
            if (lines > MAX_LINES) {
                issues.add(new AstIssue(
                    id(), name(), defaultSeverity(),
                    String.format("Method '%s' is %d lines long (max %d)",
                                  m.getNameAsString(), lines, MAX_LINES),
                    m.getBegin().map(p -> p.line).orElse(0),
                    m.getBegin().map(p -> p.column).orElse(0),
                    Map.of("methodName", m.getNameAsString(),
                           "lineCount", lines,
                           "maxLines", MAX_LINES)
                ));
            }
        }
        return issues;
    }
}
