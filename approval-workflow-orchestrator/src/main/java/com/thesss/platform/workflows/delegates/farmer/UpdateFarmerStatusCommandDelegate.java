package com.thesss.platform.workflows.delegates.farmer;

import com.thesss.platform.workflows.client.farmer.FarmerServiceClient;
import com.thesss.platform.workflows.client.farmer.dto.UpdateFarmerStatusRequestDto;
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
@Component("updateFarmerStatusCommandDelegate")
@RequiredArgsConstructor
public class UpdateFarmerStatusCommandDelegate implements JavaDelegate {

    private final FarmerServiceClient farmerServiceClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing UpdateFarmerStatusCommandDelegate for process instance {}", execution.getProcessInstanceId());

        ApprovalRequestData approvalRequestData = (ApprovalRequestData) execution.getVariable(ProcessConstants.VAR_APPROVAL_REQUEST_DATA);
        ApprovalResultData approvalResultData = (ApprovalResultData) execution.getVariable(ProcessConstants.VAR_APPROVAL_RESULT_DATA);

        if (approvalRequestData == null || approvalResultData == null) {
            log.error("Required process variables (ApprovalRequestData or ApprovalResultData) not found for instance {}", execution.getProcessInstanceId());
            throw new BpmnError("VAR_MISSING_ERROR", "Required process variables not found.");
        }

        String farmerId = approvalRequestData.getEntityId();
        boolean isApproved = approvalResultData.isApproved();
        String newStatus = isApproved ? ProcessConstants.STATUS_APPROVED : ProcessConstants.STATUS_REJECTED;

        UpdateFarmerStatusRequestDto requestDto = new UpdateFarmerStatusRequestDto();
        requestDto.setStatus(newStatus);
        // Potentially add more details like reason for rejection from approvalResultData.getComments()
        // requestDto.setComments(approvalResultData.getComments());


        try {
            log.info("Calling FarmerServiceClient to update status for farmerId: {} to status: {}", farmerId, newStatus);
            farmerServiceClient.updateFarmerStatus(farmerId, requestDto);
            log.info("Successfully updated status for farmerId: {}", farmerId);
        } catch (Exception e) {
            log.error("Failed to update farmer status for farmerId {}: {}", farmerId, e.getMessage(), e);
            // Throw BpmnError to allow Camunda to handle it (e.g., incident, retry)
            // Or use a specific error code if the BPMN model has error boundary events
            throw new BpmnError("FARMER_SERVICE_CALL_FAILED", "Failed to update farmer status: " + e.getMessage());
        }
    }
}