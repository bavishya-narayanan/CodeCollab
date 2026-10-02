package com.codecollab.snippet.exception;

public class SnippetNotFoundException extends RuntimeException {
    public SnippetNotFoundException(Long id) {
        super("Snippet not found: " + id);
    }
}
