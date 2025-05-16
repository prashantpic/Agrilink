package com.thesss.platform.farmer.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object to represent an encrypted string explicitly.
 * REQ-FRM-010, REQ-FRM-011
 */
public final class EncryptedValue implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String value; // The encrypted string

    // Private constructor, use factory method
    private EncryptedValue(String value) {
        this.value = Objects.requireNonNull(value, "Encrypted value string cannot be null.");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Encrypted value string cannot be blank.");
        }
    }

    public static EncryptedValue of(String encryptedString) {
        return new EncryptedValue(encryptedString);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EncryptedValue that = (EncryptedValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        // Deliberately do not expose the encrypted value in toString()
        // to prevent accidental logging of sensitive data.
        return "EncryptedValue[******]";
    }
}