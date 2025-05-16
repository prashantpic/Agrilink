package com.thesss.platform.workflows.model.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequestData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String entityId;
    private String entityType;
    private String submittedByUserId;
    private OffsetDateTime submissionDate;
    private List<FieldChangeData> changes;
    private String workflowDefinitionKey;
    private String businessKey;
}