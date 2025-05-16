package com.thesss.platform.farmer.exception;

public class DataEncryptionException extends RuntimeException {

    public DataEncryptionException(String message) {
        super(message);
    }

    public DataEncryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}