package com.thesss.platform.workflows.model.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResultData implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean approved;
    private String approverUserId;
    private OffsetDateTime approvalDate;
    private String comments;
}