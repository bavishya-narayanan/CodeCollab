package com.codecollab.ast.rule;

import com.codecollab.ast.dto.AstIssue;
import java.util.List;

public interface AstRule {

    String id();
    String name();
    Severity defaultSeverity();

    /** Called once per analyzed snippet. */
    List<AstIssue> analyze(AstRuleContext context);

    /** Whether this rule applies to the given language. */
    default boolean supports(String language) {
        return "JAVA".equalsIgnoreCase(language);
    }
}
