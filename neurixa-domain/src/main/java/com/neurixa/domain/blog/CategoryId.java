package com.neurixa.domain.blog;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object for category identity.
 * Java 21 record — equals, hashCode, and accessor generated automatically.
 */
public record CategoryId(UUID value) implements Serializable {

    public CategoryId {
        Objects.requireNonNull(value, "CategoryId cannot be null.");
    }

    public static CategoryId generate() {
        return new CategoryId(UUID.randomUUID());
    }

    /** Convenience accessor matching the old getValue() call sites. */
    public UUID getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
