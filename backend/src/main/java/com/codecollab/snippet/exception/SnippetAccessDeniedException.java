package com.codecollab.snippet.exception;

public class SnippetAccessDeniedException extends RuntimeException {
    public SnippetAccessDeniedException() {
        super("You do not have permission to modify this snippet");
    }
}
