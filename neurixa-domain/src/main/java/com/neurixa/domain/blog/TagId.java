package com.neurixa.domain.blog;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object for tag identity.
 * Java 21 record — equals, hashCode, and accessor generated automatically.
 */
public record TagId(UUID value) implements Serializable {

    public TagId {
        Objects.requireNonNull(value, "TagId cannot be null.");
    }

    public static TagId generate() {
        return new TagId(UUID.randomUUID());
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
