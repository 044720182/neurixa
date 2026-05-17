package com.neurixa.domain.blog;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object for comment identity.
 * Java 21 record — equals, hashCode, and accessor generated automatically.
 */
public record CommentId(UUID value) implements Serializable {

    public CommentId {
        Objects.requireNonNull(value, "CommentId cannot be null.");
    }

    public static CommentId generate() {
        return new CommentId(UUID.randomUUID());
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
