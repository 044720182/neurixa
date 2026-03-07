package com.neurixa.domain.blog;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class ArticleId implements Serializable {
    private final UUID value;

    public ArticleId(UUID value) {
        this.value = Objects.requireNonNull(value, "ArticleId cannot be null.");
    }

    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleId articleId = (ArticleId) o;
        return value.equals(articleId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
