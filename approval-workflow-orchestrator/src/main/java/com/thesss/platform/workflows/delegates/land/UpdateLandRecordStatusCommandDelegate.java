package com.thesss.platform.workflows.delegates.land;

import com.thesss.platform.workflows.client.land.LandServiceClient;
import com.thesss.platform.workflows.client.land.dto.UpdateLandRecordStatusRequestDto;
import com.thesss.platform.workflows.model.process.ApprovalRequestData;
import com.thesss.platform.workflows.model.process.ApprovalResultData;
import com.thesss.platform.workflows.utils.ProcessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("updateLandRecordStatusCommandDelegate")
@RequiredArgsConstructor
public class UpdateLandRecordStatusCommandDelegate implements JavaDelegate {

    private final LandServiceClient landServiceClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing UpdateLandRecordStatusCommandDelegate for process instance {}", execution.getProcessInstanceId());

        ApprovalRequestData approvalRequestData = (ApprovalRequestData) execution.getVariable(ProcessConstants.VAR_APPROVAL_REQUEST_DATA);
        ApprovalResultData approvalResultData = (ApprovalResultData) execution.getVariable(ProcessConstants.VAR_APPROVAL_RESULT_DATA);

        if (approvalRequestData == null || approvalResultData == null) {
            log.error("Required process variables (ApprovalRequestData or ApprovalResultData) not found for instance {}", execution.getProcessInstanceId());
            throw new BpmnError("VAR_MISSING_ERROR", "Required process variables not found.");
        }

        String landRecordId = approvalRequestData.getEntityId();
        boolean isApproved = approvalResultData.isApproved();
        String newStatus = isApproved ? ProcessConstants.STATUS_APPROVED : ProcessConstants.STATUS_REJECTED;

        UpdateLandRecordStatusRequestDto requestDto = new UpdateLandRecordStatusRequestDto();
        requestDto.setStatus(newStatus);
        // requestDto.setComments(approvalResultData.getComments());

        try {
            log.info("Calling LandServiceClient to update status for landRecordId: {} to status: {}", landRecordId, newStatus);
            landServiceClient.updateLandRecordStatus(landRecordId, requestDto);
            log.info("Successfully updated status for landRecordId: {}", landRecordId);
        } catch (Exception e) {
            log.error("Failed to update land record status for landRecordId {}: {}", landRecordId, e.getMessage(), e);
            throw new BpmnError("LAND_SERVICE_CALL_FAILED", "Failed to update land record status: " + e.getMessage());
        }
    }
}