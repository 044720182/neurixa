package com.neurixa.core.files.exception;

import com.neurixa.core.exception.DomainException;

public class FolderOwnershipException extends DomainException {
    public FolderOwnershipException(String message) {
        super(message);
    }
}
