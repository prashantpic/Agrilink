package com.thesss.platform.workflows.delegates.land;

import com.thesss.platform.workflows.client.common.ApprovalHistoryEntryDto;
import com.thesss.platform.workflows.client.land.LandServiceClient;
import com.thesss.platform.workflows.model.process.ApprovalRequestData;
import com.thesss.platform.workflows.model.process.ApprovalResultData;
import com.thesss.platform.workflows.model.process.FieldChangeData;
import com.thesss.platform.workflows.utils.ProcessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@Slf4j
@Component("logLandApprovalHistoryDelegate")
@RequiredArgsConstructor
public class LogLandApprovalHistoryDelegate implements JavaDelegate {

    private final LandServiceClient landServiceClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing LogLandApprovalHistoryDelegate for process instance {}", execution.getProcessInstanceId());

        ApprovalRequestData approvalRequestData = (ApprovalRequestData) execution.getVariable(ProcessConstants.VAR_APPROVAL_REQUEST_DATA);
        ApprovalResultData approvalResultData = (ApprovalResultData) execution.getVariable(ProcessConstants.VAR_APPROVAL_RESULT_DATA);

        if (approvalRequestData == null || approvalResultData == null) {
            log.error("Required process variables (ApprovalRequestData or ApprovalResultData) not found for instance {}", execution.getProcessInstanceId());
            throw new BpmnError("VAR_MISSING_ERROR", "Required process variables for history logging not found.");
        }

        String landRecordId = approvalRequestData.getEntityId();
        String changedFieldsSummary = approvalRequestData.getChanges() != null ?
                approvalRequestData.getChanges().stream()
                        .map(FieldChangeData::getFieldName)
                        .collect(Collectors.joining(", "))
                : "N/A";
        
        ApprovalHistoryEntryDto historyEntryDto = ApprovalHistoryEntryDto.builder()
                .entityId(landRecordId)
                .entityType(ProcessConstants.ENTITY_TYPE_LAND_RECORD)
                .workflowInstanceId(execution.getProcessInstanceId())
                .taskId(execution.getActivityInstanceId())
                .changedByUserId(approvalRequestData.getSubmittedByUserId())
                .changeRequestDate(approvalRequestData.getSubmissionDate())
                .approvedByUserId(approvalResultData.getApproverUserId())
                .approvalDate(approvalResultData.getApprovalDate())
                .status(approvalResultData.isApproved() ? ProcessConstants.STATUS_APPROVED : ProcessConstants.STATUS_REJECTED)
                .comments(approvalResultData.getComments())
                .changedFields(approvalRequestData.getChanges())
                .description("Land Record Change Approval Workflow decision: " + (approvalResultData.isApproved() ? "Approved" : "Rejected") + ". Fields: " + changedFieldsSummary)
                .timestamp(OffsetDateTime.now())
                .build();

        try {
            log.info("Calling LandServiceClient to log approval history for landRecordId: {}", landRecordId);
            landServiceClient.logApprovalHistory(landRecordId, historyEntryDto);
            log.info("Successfully logged approval history for landRecordId: {}", landRecordId);
        } catch (Exception e) {
            log.error("Failed to log land record approval history for landRecordId {}: {}", landRecordId, e.getMessage(), e);
            throw new BpmnError("LAND_HISTORY_LOG_FAILED", "Failed to log land record approval history: " + e.getMessage());
        }
    }
}