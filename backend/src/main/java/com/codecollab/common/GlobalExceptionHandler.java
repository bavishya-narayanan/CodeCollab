package com.codecollab.common;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.codecollab.chat.exception.RateLimitExceededException;
import com.codecollab.chat.exception.RoomAccessDeniedException;
import com.codecollab.chat.exception.RoomNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised exception handler for all REST controllers.
 *
 * <p>Catches known exception types at the web layer before they reach the
 * default Spring error handler, ensuring every error response conforms to
 * the {@link ApiResponse} envelope and returns the correct HTTP status code.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // 400 — Validation failures
    // -------------------------------------------------------------------------

    /**
     * Handles {@code @Valid} constraint violations on request bodies.
     * Extracts all field-level errors into a map so clients know exactly
     * which fields failed and why, without parsing an HTML error page.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Validation failed",
                        // Last one wins when the same field has multiple violations
                        (existing, replacement) -> replacement
                ));

        log.warn("Validation failed: {}", fieldErrors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed: " + fieldErrors));
    }

    /**
     * Handles illegal business arguments — e.g., invalid enum values or
     * domain rule violations expressed as {@link IllegalArgumentException}.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 404 — Resource not found
    // -------------------------------------------------------------------------

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(com.codecollab.snippet.exception.SnippetNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSnippetNotFound(com.codecollab.snippet.exception.SnippetNotFoundException ex) {
        log.warn("Snippet not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(com.codecollab.comment.exception.CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentNotFound(com.codecollab.comment.exception.CommentNotFoundException ex) {
        log.warn("Comment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoomNotFound(RoomNotFoundException ex) {
        log.warn("Room not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 401 — Unauthorized
    // -------------------------------------------------------------------------
    
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid credentials"));
    }

    // -------------------------------------------------------------------------
    // 403 — Forbidden
    // -------------------------------------------------------------------------
    
    @ExceptionHandler({DisabledException.class, AccessDeniedException.class, com.codecollab.snippet.exception.SnippetAccessDeniedException.class})
    public ResponseEntity<ApiResponse<Void>> handleForbidden(Exception ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : "Access denied"));
    }

    @ExceptionHandler(com.codecollab.comment.exception.CommentEditWindowExpiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentEditWindowExpired(com.codecollab.comment.exception.CommentEditWindowExpiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(com.codecollab.vote.exception.CannotVoteOwnSnippetException.class)
    public ResponseEntity<ApiResponse<Void>> handleCannotVoteOwnSnippet(com.codecollab.vote.exception.CannotVoteOwnSnippetException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RoomAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoomAccessDenied(RoomAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResponse.error(ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 409 — Conflict
    // -------------------------------------------------------------------------
    
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DuplicateKeyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("Resource already exists"));
    }
    
    // -------------------------------------------------------------------------
    // 400 — Bad Request
    // -------------------------------------------------------------------------

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(com.codecollab.comment.exception.CommentDepthExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentDepthExceeded(com.codecollab.comment.exception.CommentDepthExceededException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 422 — Unprocessable Entity
    // -------------------------------------------------------------------------

    @ExceptionHandler(com.codecollab.ast.exception.AstParsingException.class)
    public ResponseEntity<ApiResponse<Void>> handleAstParsing(com.codecollab.ast.exception.AstParsingException ex) {
        log.warn("AST Parsing failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // -------------------------------------------------------------------------
    // 500 — Unexpected failures
    // -------------------------------------------------------------------------

    /**
     * Catch-all for anything not handled above.
     * Logs the full stack trace so the problem is visible in logs,
     * but returns only a generic message to the client to avoid leaking internals.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred. Please try again later."));
    }
}
