package com.thesss.platform.farmer.domain.model;

/**
 * Enum representing farmer gender.
 * REQ-FRM-005
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Gender fromString(String genderStr) {
        for (Gender gender : Gender.values()) {
            if (gender.name().equalsIgnoreCase(genderStr) || gender.getDisplayName().equalsIgnoreCase(genderStr)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown gender: " + genderStr);
    }
}