package com.neurixa.core.domain;

import com.neurixa.core.exception.InvalidUserStateException;

import java.io.Serializable;

/**
 * Value object for user identity.
 * Java 21 record — equals, hashCode, toString, and accessor generated automatically.
 */
public record UserId(String value) implements Serializable {

    // Compact constructor — validation runs before field assignment
    public UserId {
        if (value == null || value.isBlank()) {
            throw new InvalidUserStateException("User ID cannot be null or blank");
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
