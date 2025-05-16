package com.thesss.platform.workflows.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private String id;
    private String name;
    private String assignee;
    private OffsetDateTime createdDate;
    private OffsetDateTime dueDate;
    private OffsetDateTime followUpDate;
    private String description;
    private String processInstanceId;
    private String processDefinitionId;
    private String taskDefinitionKey;
    private String formKey;
    private Integer priority;
    // Potentially add process variables or task-local variables if needed for UI display
    // private Map<String, Object> variables;
}