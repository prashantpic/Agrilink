package com.thesss.platform.common.exception.types;

import com.thesss.platform.common.exception.model.ApiValidationError;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ValidationException extends BaseApplicationException {

    private final List<ApiValidationError> errors;

    public ValidationException(List<ApiValidationError> errors) {
        super("Validation failed", HttpStatus.BAD_REQUEST);
        this.errors = errors;
    }

    public ValidationException(String message, List<ApiValidationError> errors) {
        super(message, HttpStatus.BAD_REQUEST);
        this.errors = errors;
    }

    public List<ApiValidationError> getErrors() {
        return errors;
    }
}