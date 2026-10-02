package com.codecollab.comment.exception;

public class CommentEditWindowExpiredException extends RuntimeException {
    public CommentEditWindowExpiredException() {
        super("Edit window expired (15 minutes)");
    }
}
