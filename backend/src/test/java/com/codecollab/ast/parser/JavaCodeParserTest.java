package com.codecollab.ast.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaCodeParserTest {

    private final JavaCodeParser parser = new JavaCodeParser();

    @Test
    void parseValidClassSuccess() {
        String code = "public class A { public void test() {} }";
        ParsedSnippet result = parser.parse(code);
        assertTrue(result.parsedSuccessfully());
    }

    @Test
    void parseEmptyFailure() {
        ParsedSnippet result = parser.parse("");
        assertFalse(result.parsedSuccessfully());
    }

    @Test
    void parseInvalidSyntaxFailure() {
        ParsedSnippet result = parser.parse("public class A { public void ");
        assertFalse(result.parsedSuccessfully());
    }

    @Test
    void parseNullFailure() {
        ParsedSnippet result = parser.parse(null);
        assertFalse(result.parsedSuccessfully());
    }
}
