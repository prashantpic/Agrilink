package com.thesss.platform.farmer.infrastructure.persistence.converter;

import com.thesss.platform.farmer.domain.model.EncryptedValue; // Assuming domain model exists
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

// autoApply = false is generally recommended for explicit use on sensitive fields.
// If set to true, it would apply to all attributes of type EncryptedValue.
@Converter(autoApply = false)
public class JpaEncryptionConverter implements AttributeConverter<EncryptedValue, String> {

    // This converter DOES NOT perform encryption/decryption.
    // It merely converts an EncryptedValue object (which already holds an encrypted string)
    // to its underlying String representation for database storage, and vice-versa.
    // The actual encryption/decryption is handled by EncryptionService in the application/domain layers.

    @Override
    public String convertToDatabaseColumn(EncryptedValue attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getValue(); // EncryptedValue holds the already encrypted string
    }

    @Override
    public EncryptedValue convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return EncryptedValue.of(dbData); // Wrap the string from DB into EncryptedValue
    }
}