package com.thesss.platform.workflows.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTaskRequestDto {
    // General variables to be set on task completion
    private Map<String, Object> variables;

    // Specific fields often used in approval tasks
    private Boolean approved; // For approval/rejection
    private String comments;  // For approver comments
}