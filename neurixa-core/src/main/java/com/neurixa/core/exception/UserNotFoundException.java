package com.neurixa.core.exception;

/**
 * Exception thrown when a user is not found in the repository.
 * 
 * This is a specific domain exception for user lookup failures.
 */
public class UserNotFoundException extends DomainException {
    private static final String ERROR_CODE = "USER_NOT_FOUND";

    public UserNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
