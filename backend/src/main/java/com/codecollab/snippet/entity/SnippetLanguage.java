package com.codecollab.snippet.entity;

public enum SnippetLanguage {
    JAVA,
    PYTHON,
    JAVASCRIPT,
    TYPESCRIPT,
    GO,
    RUST,
    C,
    CPP,
    CSHARP,
    KOTLIN,
    SCALA,
    RUBY,
    PHP,
    SWIFT,
    SQL,
    BASH,
    HTML,
    CSS,
    JSON,
    XML,
    YAML,
    MARKDOWN,
    OTHER;

    public static SnippetLanguage fromString(String value) {
        try {
            return SnippetLanguage.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return OTHER;
        }
    }
}
