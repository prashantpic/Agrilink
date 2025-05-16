package com.thesss.platform.farmer.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Value object representing a farmer's full name.
 * REQ-FRM-003
 */
public final class FullName implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String firstName;
    private final String middleName; // Optional
    private final String lastName;

    public FullName(String firstName, String middleName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank.");
        }
        this.firstName = firstName.trim();
        this.middleName = (middleName != null && !middleName.isBlank()) ? middleName.trim() : null;
        this.lastName = lastName.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getConcatenatedFullName() {
        StringJoiner joiner = new StringJoiner(" ");
        joiner.add(firstName);
        if (middleName != null) {
            joiner.add(middleName);
        }
        joiner.add(lastName);
        return joiner.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullName fullName = (FullName) o;
        return Objects.equals(firstName, fullName.firstName) &&
               Objects.equals(middleName, fullName.middleName) &&
               Objects.equals(lastName, fullName.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, middleName, lastName);
    }

    @Override
    public String toString() {
        return getConcatenatedFullName();
    }
}