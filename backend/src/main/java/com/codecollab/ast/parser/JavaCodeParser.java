package com.codecollab.ast.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Slf4j
public class JavaCodeParser {

    private final ParserConfiguration config;
    private final JavaParser parser;

    public JavaCodeParser() {
        this.config = new ParserConfiguration()
            .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
            .setAttributeComments(true)
            .setLexicalPreservationEnabled(false);
        this.parser = new JavaParser(config);
    }

    public ParsedSnippet parse(String code) {
        if (code == null || code.isBlank()) {
            return ParsedSnippet.failure(code, "Empty code");
        }
        try {
            ParseResult<CompilationUnit> result = parser.parse(code);
            if (!result.isSuccessful() || result.getResult().isEmpty()) {
                String msg = result.getProblems().stream()
                    .map(Problem::getVerboseMessage)
                    .limit(3)
                    .collect(Collectors.joining("; "));
                return ParsedSnippet.failure(code, msg);
            }
            return ParsedSnippet.success(result.getResult().get(), code);
        } catch (Exception ex) {
            log.warn("Java parsing failed: {}", ex.getMessage());
            return ParsedSnippet.failure(code, ex.getMessage());
        }
    }
}
