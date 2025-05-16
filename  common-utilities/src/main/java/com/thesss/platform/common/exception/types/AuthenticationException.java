package com.thesss.platform.common.exception.types;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends BaseApplicationException {

    public AuthenticationException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.UNAUTHORIZED);
    }
}