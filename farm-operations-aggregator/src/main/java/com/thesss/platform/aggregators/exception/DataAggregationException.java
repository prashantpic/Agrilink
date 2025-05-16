package com.thesss.platform.aggregators.exception;

public class DataAggregationException extends RuntimeException {

    public DataAggregationException(String message) {
        super(message);
    }

    public DataAggregationException(String message, Throwable cause) {
        super(message, cause);
    }
}