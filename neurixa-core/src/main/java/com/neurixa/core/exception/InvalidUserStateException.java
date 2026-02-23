package com.neurixa.core.exception;

public class InvalidUserStateException extends DomainException {
    private static final long serialVersionUID = 1L;

    public InvalidUserStateException(String message) {
        super(message);
    }
}
