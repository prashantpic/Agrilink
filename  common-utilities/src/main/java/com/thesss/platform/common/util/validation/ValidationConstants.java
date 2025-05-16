package com.thesss.platform.common.util.validation;

public final class ValidationConstants {

    /**
     * Basic email regex. For more robust validation, consider Apache Commons Validator or a specialized library.
     * This pattern allows most common email formats.
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

    /**
     * Basic phone regex allowing for an optional '+' and digits.
     * This is a very generic pattern. Specific country codes or formats would require more complex regex.
     * Example: +11234567890 or 1234567890
     */
    public static final String PHONE_REGEX = "^\\+?[0-9. ()-]{7,25}$"; // Allows digits, spaces, dots, hyphens, parentheses

    private ValidationConstants() {
        // restrict instantiation
    }
}