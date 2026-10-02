package com.codecollab.ast.rule;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.parser.JavaCodeParser;
import com.codecollab.ast.rule.impl.GodClassRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GodClassRuleTest {

    private final GodClassRule rule = new GodClassRule();
    private final JavaCodeParser parser = new JavaCodeParser();

    @Test
    void testGodClass() {
        StringBuilder code = new StringBuilder("public class A {");
        for (int i = 0; i < 20; i++) {
            code.append(" public void test").append(i).append("() {} ");
        }
        code.append("}");
        AstRuleContext ctx = new AstRuleContext(parser.parse(code.toString()).compilationUnit(), code.toString(), "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(1, issues.size());
    }

    @Test
    void testNormalClass() {
        String code = "public class A { public void test1() {} public void test2() {} }";
        AstRuleContext ctx = new AstRuleContext(parser.parse(code).compilationUnit(), code, "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(0, issues.size());
    }
}
