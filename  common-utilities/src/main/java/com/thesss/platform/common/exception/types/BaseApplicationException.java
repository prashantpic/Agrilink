package com.thesss.platform.common.exception.types;

import org.springframework.http.HttpStatus;

public class BaseApplicationException extends RuntimeException {

    private final HttpStatus httpStatus;

    public BaseApplicationException(String message, HttpStatus status) {
        super(message);
        this.httpStatus = status;
    }

    public BaseApplicationException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.httpStatus = status;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}