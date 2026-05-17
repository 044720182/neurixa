package com.neurixa.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response for all API errors.
 * 
 * This DTO is used by the GlobalExceptionHandler to return consistent error
 * responses to clients. All API errors follow this format.
 * 
 * Fields:
 * - errorCode: Machine-readable error code (e.g., "ARTICLE_NOT_FOUND")
 * - message: Human-readable error message
 * - context: Debugging information (null if not applicable)
 * - timestamp: When the error occurred
 * - traceId: Unique identifier for correlating logs
 * - path: The API path that was called
 * - details: Additional error details (e.g., validation errors per field)
 * 
 * Example:
 * <pre>
 * {
 *   "errorCode": "ARTICLE_NOT_FOUND",
 *   "message": "Article not found",
 *   "context": "{articleId=550e8400-e29b-41d4-a716-446655440000}",
 *   "timestamp": "2026-05-17T15:30:00Z",
 *   "traceId": "123e4567-e89b-12d3-a456-426614174000",
 *   "path": "/api/blog/articles/550e8400",
 *   "details": null
 * }
 * </pre>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String errorCode;
    private String message;
    private String context;
    private Instant timestamp;
    private String traceId;
    private String path;
    private Map<String, Object> details;

    /**
     * Create an error response with minimal required fields.
     * Timestamp is auto-set to now.
     */
    public ErrorResponse(String errorCode, String message, String traceId, String path) {
        this(errorCode, message, null, Instant.now(), traceId, path, null);
    }

    /**
     * Create an error response with context and details.
     */
    public ErrorResponse(String errorCode, String message, String context, String traceId, String path, Object details) {
        this.errorCode = errorCode;
        this.message = message;
        this.context = context;
        this.timestamp = Instant.now();
        this.traceId = traceId;
        this.path = path;
        this.details = details instanceof Map ? (Map<String, Object>) details : null;
    }
}
