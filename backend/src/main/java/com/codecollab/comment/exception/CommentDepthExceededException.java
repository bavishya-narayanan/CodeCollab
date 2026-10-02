package com.codecollab.comment.exception;

public class CommentDepthExceededException extends RuntimeException {
    public CommentDepthExceededException() {
        super("Replies to replies are not allowed");
    }
}
