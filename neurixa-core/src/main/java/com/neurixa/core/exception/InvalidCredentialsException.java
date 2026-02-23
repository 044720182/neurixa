package com.neurixa.core.exception;

public class InvalidCredentialsException extends DomainException {
    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
