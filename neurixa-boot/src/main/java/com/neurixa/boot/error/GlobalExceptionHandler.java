package com.neurixa.boot.error;

import com.neurixa.core.exception.DomainException;
import com.neurixa.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for all REST controllers.
 * 
 * Converts domain exceptions to standardized HTTP responses with error codes,
 * trace IDs for debugging, and appropriate HTTP status codes.
 * 
 * Benefits:
 * - Centralized error handling (DRY principle)
 * - Consistent error response format
 * - Automatic logging and trace ID generation
 * - Framework-agnostic domain exceptions mapped to HTTP
 * 
 * Handles:
 * 1. DomainException → 400 Bad Request with error code and context
 * 2. MethodArgumentNotValidException → 422 Unprocessable Entity with validation details
 * 3. Generic Exception → 500 Internal Server Error with trace ID only
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle domain exceptions (business logic errors).
     * 
     * Maps DomainException to 400 Bad Request with error code and context.
     * Automatically logs the error with trace ID.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex,
            WebRequest request) {
        
        String traceId = generateTraceId();
        String path = extractPath(request);
        
        log.warn("Domain exception occurred [traceId={}]: {} - {}", 
            traceId, ex.getErrorCode(), ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            formatContext(ex.getContext()),
            traceId,
            path,
            ex.getContext()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handle validation errors (malformed requests).
     * 
     * Maps MethodArgumentNotValidException to 422 Unprocessable Entity
     * with details about which fields failed validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        String traceId = generateTraceId();
        String path = extractPath(request);
        Map<String, Object> details = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });

        log.warn("Validation error occurred [traceId={}]: {}", traceId, details);

        ErrorResponse response = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            null,
            traceId,
            path,
            details
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Handle all other unexpected exceptions.
     * 
     * Maps generic Exception to 500 Internal Server Error.
     * Always logs with trace ID for debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        String traceId = generateTraceId();
        String path = extractPath(request);

        log.error("Unexpected error occurred [traceId={}]: {}", traceId, ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please contact support with trace ID: " + traceId,
            traceId,
            path
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * Generate a unique trace ID for this request.
     * Used to correlate logs across services.
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Extract the request path from WebRequest.
     */
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description.replace("uri=", "");
    }

    /**
     * Format context map as a single string for display.
     */
    private String formatContext(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return null;
        }
        return context.toString();
    }
}
