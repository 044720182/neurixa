package com.neurixa.core.exception;

/**
 * Thrown when an operation violates a domain business rule
 * (e.g. publishing a deleted article, archiving a draft).
 * Maps to HTTP 422 Unprocessable Entity.
 */
public class BusinessRuleViolationException extends DomainException {
    private static final long serialVersionUID = 1L;

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
