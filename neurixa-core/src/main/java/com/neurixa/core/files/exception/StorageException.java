package com.neurixa.core.files.exception;

import com.neurixa.core.exception.DomainException;

public class StorageException extends DomainException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
