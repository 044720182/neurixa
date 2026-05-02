package com.neurixa.core.exception;

/**
 * Thrown when a requested resource does not exist or is not accessible
 * to the requesting user. Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends DomainException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
