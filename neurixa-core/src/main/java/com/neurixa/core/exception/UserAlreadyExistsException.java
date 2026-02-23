package com.neurixa.core.exception;

public class UserAlreadyExistsException extends DomainException {
    private static final long serialVersionUID = 1L;

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
