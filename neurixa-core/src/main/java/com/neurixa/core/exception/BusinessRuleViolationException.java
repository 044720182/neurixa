package com.neurixa.core.exception;

/**
 * Exception thrown when a domain business rule is violated.
 * 
 * Use this for invariant violations, state machine violations, etc.
 * 
 * Example:
 * <pre>
 * if (article.isArchived()) {
 *     throw new BusinessRuleViolationException(
 *         "Cannot publish archived article")
 *     .withContext("articleStatus", article.getStatus());
 * }
 * </pre>
 */
public class BusinessRuleViolationException extends DomainException {
    private static final String ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    public BusinessRuleViolationException(String message) {
        super(ERROR_CODE, message);
    }

    public BusinessRuleViolationException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
