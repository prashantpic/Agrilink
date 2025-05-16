package com.thesss.platform.aggregators.exception;

public class ReadModelNotFoundException extends RuntimeException {

    public ReadModelNotFoundException(String message) {
        super(message);
    }

    public ReadModelNotFoundException(String entityName, String id) {
        super(String.format("%s not found with id: %s", entityName, id));
    }

    public ReadModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}