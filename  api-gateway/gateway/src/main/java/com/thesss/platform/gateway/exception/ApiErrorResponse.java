package com.thesss.platform.gateway.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private long timestamp;
    private int status;
    private String error; // e.g., "Not Found", "Bad Request"
    private String message; // Detailed human-readable message
    private String path;
    private String errorCode; // Optional application-specific error code
    private List<String> details; // Optional list for validation errors or sub-errors
}