package com.codecollab.ast.rule;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.parser.JavaCodeParser;
import com.codecollab.ast.rule.impl.LongMethodRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongMethodRuleTest {

    private final LongMethodRule rule = new LongMethodRule();
    private final JavaCodeParser parser = new JavaCodeParser();

    @Test
    void testLongMethodExceedsLimit() {
        StringBuilder code = new StringBuilder("public class A { public void test() {");
        for (int i = 0; i < 35; i++) {
            code.append("\n System.out.println(\"line\");");
        }
        code.append("\n} }");

        AstRuleContext ctx = new AstRuleContext(parser.parse(code.toString()).compilationUnit(), code.toString(), "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(1, issues.size());
    }

    @Test
    void testShortMethodPasses() {
        String code = "public class A { public void test() { System.out.println(\"line\"); } }";
        AstRuleContext ctx = new AstRuleContext(parser.parse(code).compilationUnit(), code, "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(0, issues.size());
    }
}
