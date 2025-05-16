package com.thesss.platform.common.exception.types;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseApplicationException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT);
    }
}