package com.thesss.platform.common.exception.types;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BaseApplicationException {

    public AuthorizationException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }

    public AuthorizationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.FORBIDDEN);
    }
}