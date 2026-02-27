package com.neurixa.core.exception;

public class UserAlreadyExistsException extends DomainException {
    private static final long serialVersionUID = 1L;

    public enum Field {
        USERNAME,
        EMAIL
    }

    private final Field field;

    public UserAlreadyExistsException(Field field, String message) {
        super(message);
        this.field = field;
    }

    public Field getField() {
        return field;
    }
}
