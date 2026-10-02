package com.codecollab.ast.rule.impl;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.Severity;
import com.github.javaparser.ast.stmt.CatchClause;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EmptyCatchRule implements AstRule {

    @Override public String id()   { return "R003"; }
    @Override public String name() { return "EmptyCatchBlock"; }
    @Override public Severity defaultSeverity() { return Severity.ERROR; }

    @Override
    public List<AstIssue> analyze(AstRuleContext ctx) {
        List<AstIssue> issues = new ArrayList<>();
        List<CatchClause> catches = ctx.compilationUnit().findAll(CatchClause.class);
        
        for (CatchClause cc : catches) {
            if (cc.getBody().getStatements().isEmpty() && 
                cc.getBody().getAllContainedComments().isEmpty()) {
                
                String exceptionType = cc.getParameter().getType().asString();
                
                issues.add(new AstIssue(
                    id(), name(), defaultSeverity(),
                    "Empty catch block found for exception: " + exceptionType,
                    cc.getBegin().map(p -> p.line).orElse(0),
                    cc.getBegin().map(p -> p.column).orElse(0),
                    Map.of("exceptionType", exceptionType)
                ));
            }
        }
        return issues;
    }
}
