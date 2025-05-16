package com.thesss.platform.farmer.domain.model;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing farmer's consent for data usage.
 * REQ-FRM-021
 */
@Getter
public class Consent implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id; // Internal ID for the consent record
    private final boolean consentGiven;
    private final LocalDateTime consentTimestamp;
    private final String consentPurpose;
    private final String consentVersionId;

    public Consent(boolean consentGiven, String consentPurpose, String consentVersionId) {
        this.id = UUID.randomUUID(); // Generate ID upon creation
        if (consentPurpose == null || consentPurpose.isBlank()) {
            throw new IllegalArgumentException("Consent purpose cannot be blank.");
        }
        if (consentVersionId == null || consentVersionId.isBlank()) {
            throw new IllegalArgumentException("Consent version ID cannot be blank.");
        }
        this.consentGiven = consentGiven;
        this.consentTimestamp = LocalDateTime.now();
        this.consentPurpose = consentPurpose;
        this.consentVersionId = consentVersionId;
    }
    
    // Constructor for loading from persistence
    public Consent(UUID id, boolean consentGiven, LocalDateTime consentTimestamp, String consentPurpose, String consentVersionId) {
        this.id = Objects.requireNonNull(id);
        if (consentPurpose == null || consentPurpose.isBlank()) {
            throw new IllegalArgumentException("Consent purpose cannot be blank.");
        }
        if (consentVersionId == null || consentVersionId.isBlank()) {
            throw new IllegalArgumentException("Consent version ID cannot be blank.");
        }
        this.consentGiven = consentGiven;
        this.consentTimestamp = Objects.requireNonNull(consentTimestamp);
        this.consentPurpose = consentPurpose;
        this.consentVersionId = consentVersionId;
    }


    // Example method: Check if consent is active (could be more complex, e.g., check against current version)
    public boolean isActiveForPurpose(String purpose, String currentVersion) {
        return this.consentGiven &&
               this.consentPurpose.equalsIgnoreCase(purpose) &&
               this.consentVersionId.equalsIgnoreCase(currentVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consent consent = (Consent) o;
        return Objects.equals(id, consent.id); // Entity equality by ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Consent{" +
               "id=" + id +
               ", consentGiven=" + consentGiven +
               ", consentTimestamp=" + consentTimestamp +
               ", consentPurpose='" + consentPurpose + '\'' +
               ", consentVersionId='" + consentVersionId + '\'' +
               '}';
    }
}