package com.neurixa.core.exception;

public class InvalidUserStateException extends DomainException {
    public InvalidUserStateException(String message) {
        super(message);
    }
}
