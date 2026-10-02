package com.codecollab.ast.rule;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.parser.JavaCodeParser;
import com.codecollab.ast.rule.impl.MagicNumberRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MagicNumberRuleTest {

    private final MagicNumberRule rule = new MagicNumberRule();
    private final JavaCodeParser parser = new JavaCodeParser();

    @Test
    void testMagicNumber() {
        String code = "public class A { public void test() { if (x > 42) {} } }";
        AstRuleContext ctx = new AstRuleContext(parser.parse(code).compilationUnit(), code, "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(1, issues.size());
    }

    @Test
    void testExemptNumbers() {
        String code = "public class A { public void test() { if (x > 0 || y == 1 || z == -1) {} } }";
        AstRuleContext ctx = new AstRuleContext(parser.parse(code).compilationUnit(), code, "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(0, issues.size());
    }
}
