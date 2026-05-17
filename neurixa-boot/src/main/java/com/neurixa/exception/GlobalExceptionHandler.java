package com.neurixa.exception;

import com.neurixa.core.exception.BusinessRuleViolationException;
import com.neurixa.core.exception.DomainException;
import com.neurixa.core.exception.InvalidCredentialsException;
import com.neurixa.core.exception.InvalidUserStateException;
import com.neurixa.core.exception.ResourceNotFoundException;
import com.neurixa.core.exception.UserAlreadyExistsException;
import com.neurixa.core.exception.UserNotFoundException;
import com.neurixa.core.files.exception.FileValidationException;
import com.neurixa.core.files.exception.FolderOwnershipException;
import com.neurixa.domain.blog.exception.ArticleNotFoundException;
import com.neurixa.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── 409 Conflict ─────────────────────────────────────────────────────────

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request);
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(ArticleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleArticleNotFound(
            ArticleNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request);
    }

    // ── 422 Unprocessable Entity ──────────────────────────────────────────────

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleViolation(
            BusinessRuleViolationException ex, HttpServletRequest request) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage(), request);
    }

    /**
     * Safety net for IllegalStateException thrown from domain entities
     * (e.g. "Cannot publish a deleted article", "Only a pending comment can be approved").
     * These are business rule violations that should return 422, not 500.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {
        log.warn("Business rule violation at {}: {}", request.getRequestURI(), ex.getMessage());
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable Entity", ex.getMessage(), request);
    }

    // ── 401 Unauthorized ──────────────────────────────────────────────────────

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request);
    }

    // ── 400 Bad Request ───────────────────────────────────────────────────────

    @ExceptionHandler(InvalidUserStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserState(
            InvalidUserStateException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    @ExceptionHandler({FileValidationException.class, FolderOwnershipException.class})
    public ResponseEntity<ErrorResponse> handleFileExceptions(
            DomainException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    /**
     * Safety net for IllegalArgumentException thrown from domain entities and use cases
     * (e.g. "Title cannot be empty", "File not found" before migration to typed exceptions).
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid argument at {}: {}", request.getRequestURI(), ex.getMessage());
        return error(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid input parameters",
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = "Required parameter '" + ex.getParameterName() + "' is missing";
        return error(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Parameter '" + ex.getName() + "' has invalid value: '" + ex.getValue() + "'";
        return error(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    /**
     * Catch-all for unhandled DomainException subtypes.
     * Should not normally be reached — add a specific handler if a new exception type is introduced.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex, HttpServletRequest request) {
        log.warn("Unhandled domain exception at {}: {}", request.getRequestURI(), ex.getMessage());
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String error,
                                                  String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                new ErrorResponse(status.value(), error, message, request.getRequestURI()));
    }
}
