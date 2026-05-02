package com.neurixa.domain.blog;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object for article identity.
 * Java 21 record — equals, hashCode, and accessor generated automatically.
 */
public record ArticleId(UUID value) implements Serializable {

    public ArticleId {
        Objects.requireNonNull(value, "ArticleId cannot be null.");
    }

    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID());
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
