package com.neurixa.core.exception;

public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
