package com.neurixa.core.files.domain;

import java.util.Objects;

public final class Checksum {
    private final String algorithm;
    private final String value;

    public Checksum(String algorithm, String value) {
        if (algorithm == null || algorithm.isBlank()) {
            throw new IllegalArgumentException("Algorithm cannot be null or blank");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Checksum value cannot be null or blank");
        }
        this.algorithm = algorithm;
        this.value = value;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Checksum checksum = (Checksum) o;
        return algorithm.equalsIgnoreCase(checksum.algorithm) && value.equals(checksum.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm.toLowerCase(), value);
    }
}

