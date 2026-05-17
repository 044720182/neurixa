package com.neurixa.core.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base exception for all domain-level errors in the application.
 * 
 * This exception is framework-agnostic and designed to be caught at the
 * application/adapter boundary and converted to HTTP responses.
 * 
 * Benefits:
 * - Structured error codes instead of string messages
 * - Context map for attaching debugging information
 * - Immutable, thread-safe context
 * - Method chaining for fluent API
 * 
 * Example:
 * <pre>
 * throw new ArticleNotFoundException("Article with ID not found")
 *     .withContext("articleId", articleId.getValue())
 *     .withContext("userId", userId.getValue());
 * </pre>
 */
public class DomainException extends RuntimeException {
    private final String errorCode;
    private final Map<String, Object> context;

    /**
     * Create a domain exception with an error code and message.
     * 
     * @param errorCode The machine-readable error code (e.g., "ARTICLE_NOT_FOUND")
     * @param message The human-readable error message
     */
    public DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }

    /**
     * Create a domain exception with an error code, message, and cause.
     * 
     * @param errorCode The machine-readable error code
     * @param message The human-readable error message
     * @param cause The underlying cause exception
     */
    public DomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = new HashMap<>();
    }

    /**
     * Get the machine-readable error code.
     * 
     * @return The error code (e.g., "ARTICLE_NOT_FOUND")
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Get the immutable context map.
     * 
     * @return An unmodifiable map of context information
     */
    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }

    /**
     * Add context information to this exception.
     * Enables fluent API for building rich exception information.
     * 
     * @param key The context key
     * @param value The context value
     * @return This exception for method chaining
     */
    public DomainException withContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    /**
     * Add multiple context items from a map.
     * 
     * @param contextMap The context map to add
     * @return This exception for method chaining
     */
    public DomainException withAllContext(Map<String, Object> contextMap) {
        if (contextMap != null) {
            this.context.putAll(contextMap);
        }
        return this;
    }

    @Override
    public String toString() {
        return "DomainException{" +
                "errorCode='" + errorCode + '\'' +
                ", message='" + getMessage() + '\'' +
                ", context=" + context +
                '}';
    }
}
