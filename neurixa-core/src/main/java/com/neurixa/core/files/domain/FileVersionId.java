package com.neurixa.core.files.domain;

import java.io.Serializable;
import java.util.Objects;

public final class FileVersionId implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String value;

    public FileVersionId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FileVersion ID cannot be null or blank");
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
        FileVersionId that = (FileVersionId) o;
        return value.equals(that.value);
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

