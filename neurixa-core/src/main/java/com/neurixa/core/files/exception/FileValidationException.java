package com.neurixa.core.files.exception;

import com.neurixa.core.exception.DomainException;

public class FileValidationException extends DomainException {
    public FileValidationException(String message) {
        super(message);
    }
}
