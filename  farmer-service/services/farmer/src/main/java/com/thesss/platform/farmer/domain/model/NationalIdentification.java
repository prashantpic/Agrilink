package com.thesss.platform.farmer.domain.model;

import com.thesss.platform.farmer.domain.service.EncryptionService;
import java.io.Serializable;
import java.util.Objects;

/**
 * Value object for National Identification details.
 * REQ-FRM-010
 */
public final class NationalIdentification implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String nationalIdType;
    private final EncryptedValue idNumber; // Encrypted

    private NationalIdentification(String nationalIdType, EncryptedValue idNumber) {
        if (nationalIdType == null || nationalIdType.isBlank()) {
            // If type is mandatory when ID is present. This might be relaxed if both can be null.
            // throw new IllegalArgumentException("National ID type cannot be blank if ID number is provided.");
        }
        this.nationalIdType = nationalIdType;
        this.idNumber = idNumber; // Can be null if no ID is provided
    }

    public static NationalIdentification create(String nationalIdType, String idNumberPlaintext, EncryptionService encryptionService) {
        Objects.requireNonNull(encryptionService, "EncryptionService cannot be null for creating NationalIdentification.");
        if (idNumberPlaintext == null || idNumberPlaintext.isBlank()) {
            return new NationalIdentification(nationalIdType, null);
        }
        EncryptedValue encryptedIdNumber = encryptionService.encrypt(idNumberPlaintext);
        return new NationalIdentification(nationalIdType, encryptedIdNumber);
    }
    
    // Factory method to create from an already encrypted value (e.g. when loading from DB)
    public static NationalIdentification fromEncrypted(String nationalIdType, EncryptedValue encryptedIdNumber) {
        return new NationalIdentification(nationalIdType, encryptedIdNumber);
    }


    public String getNationalIdType() {
        return nationalIdType;
    }

    public EncryptedValue getIdNumber() {
        return idNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NationalIdentification that = (NationalIdentification) o;
        return Objects.equals(nationalIdType, that.nationalIdType) &&
               Objects.equals(idNumber, that.idNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nationalIdType, idNumber);
    }

    @Override
    public String toString() {
        return "NationalIdentification{" +
               "nationalIdType='" + nationalIdType + '\'' +
               // DO NOT print idNumber value, even if encrypted
               ", idNumber=" + (idNumber != null ? "PRESENT" : "NOT_PRESENT") +
               '}';
    }
}