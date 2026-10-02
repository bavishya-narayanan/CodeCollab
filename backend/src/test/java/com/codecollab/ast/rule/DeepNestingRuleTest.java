package com.codecollab.ast.rule;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.parser.JavaCodeParser;
import com.codecollab.ast.rule.impl.DeepNestingRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeepNestingRuleTest {

    private final DeepNestingRule rule = new DeepNestingRule();
    private final JavaCodeParser parser = new JavaCodeParser();

    @Test
    void testDeepNesting() {
        String code = "public class A { public void test() { if (true) { if (true) { if (true) { if (true) { if (true) {} } } } } } }";
        AstRuleContext ctx = new AstRuleContext(parser.parse(code).compilationUnit(), code, "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(1, issues.size());
    }

    @Test
    void testShallowNesting() {
        String code = "public class A { public void test() { if (true) { if (true) {} } } }";
        AstRuleContext ctx = new AstRuleContext(parser.parse(code).compilationUnit(), code, "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(0, issues.size());
    }
}
