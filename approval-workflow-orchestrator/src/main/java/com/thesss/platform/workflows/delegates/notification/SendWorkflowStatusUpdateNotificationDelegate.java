package com.thesss.platform.workflows.delegates.notification;

import com.thesss.platform.workflows.client.notification.NotificationServiceClient;
import com.thesss.platform.workflows.client.notification.dto.NotificationRequestDto;
import com.thesss.platform.workflows.model.process.ApprovalRequestData;
import com.thesss.platform.workflows.model.process.ApprovalResultData;
import com.thesss.platform.workflows.utils.ProcessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component("sendWorkflowStatusUpdateNotificationDelegate")
@RequiredArgsConstructor
public class SendWorkflowStatusUpdateNotificationDelegate implements JavaDelegate {

    private final NotificationServiceClient notificationServiceClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing SendWorkflowStatusUpdateNotificationDelegate for process instance {}", execution.getProcessInstanceId());

        ApprovalRequestData approvalRequestData = (ApprovalRequestData) execution.getVariable(ProcessConstants.VAR_APPROVAL_REQUEST_DATA);
        ApprovalResultData approvalResultData = (ApprovalResultData) execution.getVariable(ProcessConstants.VAR_APPROVAL_RESULT_DATA);

        if (approvalRequestData == null || approvalResultData == null) {
            log.error("ApprovalRequestData or ApprovalResultData not found for instance {}. Cannot send status update notification.", execution.getProcessInstanceId());
            // Potentially non-critical, or could be a BpmnError if notification is mandatory
            return; // Or throw new BpmnError("NOTIFICATION_VAR_MISSING", "Missing variables for status update notification.");
        }

        String submitterUserId = approvalRequestData.getSubmittedByUserId();
        if (submitterUserId == null) {
            log.warn("SubmitterUserId not found for process instance {}. Cannot send status update.", execution.getProcessInstanceId());
            return;
        }

        String status = approvalResultData.isApproved() ? ProcessConstants.STATUS_APPROVED : ProcessConstants.STATUS_REJECTED;

        Map<String, String> payload = new HashMap<>();
        payload.put("entityId", approvalRequestData.getEntityId());
        payload.put("entityType", approvalRequestData.getEntityType());
        payload.put("status", status);
        payload.put("comments", approvalResultData.getComments() != null ? approvalResultData.getComments() : "");
        payload.put("processInstanceId", execution.getProcessInstanceId());

        NotificationRequestDto notificationRequest = NotificationRequestDto.builder()
                .recipientId(submitterUserId)
                .recipientType(ProcessConstants.RECIPIENT_TYPE_USER)
                .channelType(ProcessConstants.NOTIFICATION_CHANNEL_EMAIL) // Or preferred channel
                .templateId(status.equals(ProcessConstants.STATUS_APPROVED) ? ProcessConstants.TEMPLATE_ID_WORKFLOW_APPROVED : ProcessConstants.TEMPLATE_ID_WORKFLOW_REJECTED)
                .payload(payload)
                .build();

        try {
            log.info("Sending workflow status update notification to submitter {} for entity {}", submitterUserId, approvalRequestData.getEntityId());
            notificationServiceClient.sendNotification(notificationRequest);
            log.info("Successfully sent workflow status update notification for process instance {}", execution.getProcessInstanceId());
        } catch (Exception e) {
            log.error("Failed to send workflow status update notification for process instance {}: {}", execution.getProcessInstanceId(), e.getMessage(), e);
            // Depending on criticality, either log and continue or throw BpmnError
            // throw new BpmnError("NOTIFICATION_SERVICE_FAILED", "Failed to send workflow status update notification: " + e.getMessage());
        }
    }
}