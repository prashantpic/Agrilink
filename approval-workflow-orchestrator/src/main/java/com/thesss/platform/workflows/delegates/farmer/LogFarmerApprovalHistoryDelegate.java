package com.thesss.platform.workflows.delegates.farmer;

import com.thesss.platform.workflows.client.common.ApprovalHistoryEntryDto;
import com.thesss.platform.workflows.client.farmer.FarmerServiceClient;
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
@Component("logFarmerApprovalHistoryDelegate")
@RequiredArgsConstructor
public class LogFarmerApprovalHistoryDelegate implements JavaDelegate {

    private final FarmerServiceClient farmerServiceClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing LogFarmerApprovalHistoryDelegate for process instance {}", execution.getProcessInstanceId());

        ApprovalRequestData approvalRequestData = (ApprovalRequestData) execution.getVariable(ProcessConstants.VAR_APPROVAL_REQUEST_DATA);
        ApprovalResultData approvalResultData = (ApprovalResultData) execution.getVariable(ProcessConstants.VAR_APPROVAL_RESULT_DATA);

        if (approvalRequestData == null || approvalResultData == null) {
            log.error("Required process variables (ApprovalRequestData or ApprovalResultData) not found for instance {}", execution.getProcessInstanceId());
            throw new BpmnError("VAR_MISSING_ERROR", "Required process variables for history logging not found.");
        }

        String farmerId = approvalRequestData.getEntityId();
        String changedFieldsSummary = approvalRequestData.getChanges() != null ?
                approvalRequestData.getChanges().stream()
                        .map(FieldChangeData::getFieldName)
                        .collect(Collectors.joining(", "))
                : "N/A";

        // Assuming ApprovalHistoryEntryDto is a common DTO.
        // If FarmerService expects a more specific DTO, mapping would be needed here.
        ApprovalHistoryEntryDto historyEntryDto = ApprovalHistoryEntryDto.builder()
                .entityId(farmerId)
                .entityType(ProcessConstants.ENTITY_TYPE_FARMER)
                .workflowInstanceId(execution.getProcessInstanceId())
                .taskId(execution.getActivityInstanceId()) // Or current task ID if available and relevant
                .changedByUserId(approvalRequestData.getSubmittedByUserId())
                .changeRequestDate(approvalRequestData.getSubmissionDate())
                .approvedByUserId(approvalResultData.getApproverUserId())
                .approvalDate(approvalResultData.getApprovalDate())
                .status(approvalResultData.isApproved() ? ProcessConstants.STATUS_APPROVED : ProcessConstants.STATUS_REJECTED)
                .comments(approvalResultData.getComments())
                .changedFields(approvalRequestData.getChanges()) // Passing the list of FieldChangeData
                .description("Farmer Registration Approval Workflow decision: " + (approvalResultData.isApproved() ? "Approved" : "Rejected") + ". Fields: " + changedFieldsSummary)
                .timestamp(OffsetDateTime.now())
                .build();
        
        try {
            log.info("Calling FarmerServiceClient to log approval history for farmerId: {}", farmerId);
            farmerServiceClient.logApprovalHistory(farmerId, historyEntryDto);
            log.info("Successfully logged approval history for farmerId: {}", farmerId);
        } catch (Exception e) {
            log.error("Failed to log farmer approval history for farmerId {}: {}", farmerId, e.getMessage(), e);
            // This might be a non-critical error in some contexts, logging and continuing might be an option.
            // For now, treat as a BpmnError.
            throw new BpmnError("FARMER_HISTORY_LOG_FAILED", "Failed to log farmer approval history: " + e.getMessage());
        }
    }
}