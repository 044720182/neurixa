package com.neurixa.core.files.domain;

import java.io.Serializable;

/**
 * Value object for file identity.
 * Java 21 record — equals, hashCode, toString, and accessor generated automatically.
 */
public record FileId(String value) implements Serializable {

    public FileId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("File ID cannot be null or blank");
        }
    }

    /** Convenience accessor matching the old getValue() call sites. */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
