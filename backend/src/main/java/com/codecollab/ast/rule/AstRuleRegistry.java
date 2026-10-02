package com.codecollab.ast.rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AstRuleRegistry {

    private final List<AstRule> rules;

    public List<AstRule> rulesFor(String language) {
        return rules.stream()
            .filter(r -> r.supports(language))
            .sorted(Comparator.comparing(AstRule::id))
            .toList();
    }

    public List<AstRule> allRules() {
        return List.copyOf(rules);
    }

    public String currentVersion() {
        return "v1";
    }
}
