package com.thesss.platform.common.exception.types;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseApplicationException {

    private static final String DEFAULT_MESSAGE_TEMPLATE = "%s not found with %s: '%s'";

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format(DEFAULT_MESSAGE_TEMPLATE, resourceName, fieldName, fieldValue), HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause, HttpStatus.NOT_FOUND);
    }
}