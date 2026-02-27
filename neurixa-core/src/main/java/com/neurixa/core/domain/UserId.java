package com.neurixa.core.domain;

import com.neurixa.core.exception.InvalidUserStateException;

import java.io.Serializable;
import java.util.Objects;

public final class UserId implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String value;

    public UserId(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidUserStateException("User ID cannot be null or blank");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return value.equals(userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
