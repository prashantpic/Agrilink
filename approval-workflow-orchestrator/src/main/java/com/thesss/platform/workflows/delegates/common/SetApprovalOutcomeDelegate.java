package com.thesss.platform.workflows.delegates.common;

import com.thesss.platform.workflows.model.process.ApprovalResultData;
import com.thesss.platform.workflows.utils.ProcessConstants;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component("setApprovalOutcomeDelegate")
public class SetApprovalOutcomeDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing SetApprovalOutcomeDelegate for process instance {}", execution.getProcessInstanceId());

        ApprovalResultData approvalResultData = (ApprovalResultData) execution.getVariable(ProcessConstants.VAR_APPROVAL_RESULT_DATA);

        if (approvalResultData == null) {
            log.error("ApprovalResultData not found in process variables for instance {}. Cannot set outcome.", execution.getProcessInstanceId());
            // Potentially throw BpmnError if this is a critical failure that should be handled in the process
            // For now, we'll just log and continue, but the process might fail later if these vars are expected.
            // To make it more robust, we could try to read individual variables if ApprovalResultData is missing.
            boolean isApproved = (Boolean) execution.getVariable(ProcessConstants.VAR_IS_APPROVED);
            String comments = (String) execution.getVariable(ProcessConstants.VAR_COMMENTS); // Assuming VAR_COMMENTS might be set directly
            String approverUserId = (String) execution.getVariable(ProcessConstants.VAR_APPROVER_USER_ID); // Assuming VAR_APPROVER_USER_ID might be set

             approvalResultData = ApprovalResultData.builder()
                .approved(isApproved)
                .approverUserId(approverUserId) // This should be reliable if set at task completion
                .approvalDate(OffsetDateTime.now()) // Default to now if not set
                .comments(comments)
                .build();
            // If still missing, throw error or set defaults
            if (approverUserId == null) {
                 log.warn("ApproverUserId not found. Outcome setting might be incomplete.");
            }
        }
        
        execution.setVariable(ProcessConstants.VAR_IS_APPROVED, approvalResultData.isApproved());
        execution.setVariable(ProcessConstants.VAR_APPROVER_USER_ID, approvalResultData.getApproverUserId());
        execution.setVariable(ProcessConstants.VAR_APPROVAL_COMMENTS, approvalResultData.getComments());
        execution.setVariable(ProcessConstants.VAR_APPROVAL_DATE, approvalResultData.getApprovalDate());
        // Ensure VAR_APPROVAL_RESULT_DATA is consistent or updated
        execution.setVariable(ProcessConstants.VAR_APPROVAL_RESULT_DATA, approvalResultData);


        log.info("Set approval outcome: isApproved={}, approverUserId={}, comments='{}', approvalDate={} for process instance {}",
                approvalResultData.isApproved(),
                approvalResultData.getApproverUserId(),
                approvalResultData.getComments(),
                approvalResultData.getApprovalDate(),
                execution.getProcessInstanceId());
    }
}