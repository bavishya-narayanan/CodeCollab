package com.codecollab.ast.rule;

import com.codecollab.ast.dto.AstIssue;
import com.codecollab.ast.parser.JavaCodeParser;
import com.codecollab.ast.rule.impl.EmptyCatchRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmptyCatchRuleTest {

    private final EmptyCatchRule rule = new EmptyCatchRule();
    private final JavaCodeParser parser = new JavaCodeParser();

    @Test
    void testEmptyCatchBlock() {
        String code = "public class A { public void test() { try {} catch (Exception e) {} } }";
        AstRuleContext ctx = new AstRuleContext(parser.parse(code).compilationUnit(), code, "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(1, issues.size());
    }

    @Test
    void testCatchBlockWithComment() {
        String code = "public class A { public void test() { try {} catch (Exception e) { /* ignored */ } } }";
        AstRuleContext ctx = new AstRuleContext(parser.parse(code).compilationUnit(), code, "JAVA", null);
        List<AstIssue> issues = rule.analyze(ctx);
        assertEquals(0, issues.size());
    }
}
