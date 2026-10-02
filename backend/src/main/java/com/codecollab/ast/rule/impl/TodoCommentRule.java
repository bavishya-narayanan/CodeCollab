package com.codecollab.ast.rule.impl;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.rule.AstRule;
import com.codecollab.ast.rule.AstRuleContext;
import com.codecollab.ast.rule.Severity;
import com.github.javaparser.ast.comments.Comment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TodoCommentRule implements AstRule {

    @Override public String id()   { return "R008"; }
    @Override public String name() { return "TodoComment"; }
    @Override public Severity defaultSeverity() { return Severity.INFO; }

    @Override
    public List<AstIssue> analyze(AstRuleContext ctx) {
        List<AstIssue> issues = new ArrayList<>();
        
        ctx.compilationUnit().findAll(Comment.class).forEach(comment -> {
            String content = comment.getContent().toUpperCase();
            if (content.contains("TODO") || content.contains("FIXME")) {
                issues.add(new AstIssue(
                    id(), name(), defaultSeverity(),
                    "TODO or FIXME comment detected",
                    comment.getBegin().map(p -> p.line).orElse(0),
                    comment.getBegin().map(p -> p.column).orElse(0),
                    Map.of("commentText", comment.getContent().trim())
                ));
            }
        });
        
        return issues;
    }
}
