package com.codecollab.vote.exception;

public class CannotVoteOwnSnippetException extends RuntimeException {
    public CannotVoteOwnSnippetException() {
        super("You cannot vote on your own snippet");
    }
}
