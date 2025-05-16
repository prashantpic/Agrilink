package com.thesss.platform.farmer.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object for contact details.
 * REQ-FRM-006, REQ-FRM-007
 */
public final class ContactInformation implements Serializable {
    private static final long serialVersionUID = 1L;

    // Basic E.164 like pattern, can be refined
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");
    // Basic email pattern, for more robust use a library or detailed regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");


    private final String primaryPhoneNumber;
    private final String secondaryPhoneNumber; // Optional
    private final String emailAddress; // Optional

    public ContactInformation(String primaryPhoneNumber, String secondaryPhoneNumber, String emailAddress) {
        if (primaryPhoneNumber == null || primaryPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("Primary phone number cannot be blank.");
        }
        // Example validation, can be more strict or use external lib
        // if (!PHONE_PATTERN.matcher(primaryPhoneNumber).matches()) {
        //     throw new IllegalArgumentException("Invalid primary phone number format.");
        // }

        this.primaryPhoneNumber = primaryPhoneNumber;
        this.secondaryPhoneNumber = secondaryPhoneNumber;

        if (emailAddress != null && !emailAddress.isBlank()) {
            // if (!EMAIL_PATTERN.matcher(emailAddress).matches()) {
            //     throw new IllegalArgumentException("Invalid email address format.");
            // }
            this.emailAddress = emailAddress;
        } else {
            this.emailAddress = null;
        }
    }

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public String getSecondaryPhoneNumber() {
        return secondaryPhoneNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactInformation that = (ContactInformation) o;
        return Objects.equals(primaryPhoneNumber, that.primaryPhoneNumber) &&
               Objects.equals(secondaryPhoneNumber, that.secondaryPhoneNumber) &&
               Objects.equals(emailAddress, that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryPhoneNumber, secondaryPhoneNumber, emailAddress);
    }

    @Override
    public String toString() {
        return "ContactInformation{" +
               "primaryPhoneNumber='" + primaryPhoneNumber + '\'' +
               ", secondaryPhoneNumber='" + secondaryPhoneNumber + '\'' +
               ", emailAddress='" + emailAddress + '\'' +
               '}';
    }
}