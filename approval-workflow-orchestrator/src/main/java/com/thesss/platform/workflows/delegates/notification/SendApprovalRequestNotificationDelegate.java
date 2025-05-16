package com.thesss.platform.workflows.delegates.notification;

import com.thesss.platform.workflows.client.notification.NotificationServiceClient;
import com.thesss.platform.workflows.client.notification.dto.NotificationRequestDto;
import com.thesss.platform.workflows.model.process.ApprovalRequestData;
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
@Component("sendApprovalRequestNotificationDelegate")
@RequiredArgsConstructor
public class SendApprovalRequestNotificationDelegate implements JavaDelegate {

    private final NotificationServiceClient notificationServiceClient;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing SendApprovalRequestNotificationDelegate for process instance {}", execution.getProcessInstanceId());

        String assignee = (String) execution.getVariable(ProcessConstants.VAR_ASSIGNEE); // Set by AssignApproverDelegate or User Task
        String candidateGroup = (String) execution.getVariable(ProcessConstants.VAR_CANDIDATE_GROUP); // Set by AssignApproverDelegate

        ApprovalRequestData approvalRequestData = (ApprovalRequestData) execution.getVariable(ProcessConstants.VAR_APPROVAL_REQUEST_DATA);

        if ((assignee == null && candidateGroup == null) || approvalRequestData == null) {
            log.error("Assignee/CandidateGroup or ApprovalRequestData not found for instance {}. Cannot send approval request notification.", execution.getProcessInstanceId());
            // Potentially non-critical, or could be a BpmnError if notification is mandatory
            return; // Or throw new BpmnError("NOTIFICATION_VAR_MISSING", "Missing variables for notification.");
        }

        String recipientId = assignee; // Prefer assignee if available
        String recipientType = ProcessConstants.RECIPIENT_TYPE_USER;
        if (recipientId == null) {
            recipientId = candidateGroup;
            recipientType = ProcessConstants.RECIPIENT_TYPE_GROUP;
        }
        
        if (recipientId == null) {
            log.warn("No recipient (user or group) found for approval request notification in process instance {}", execution.getProcessInstanceId());
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("entityId", approvalRequestData.getEntityId());
        payload.put("entityType", approvalRequestData.getEntityType());
        payload.put("submittedBy", approvalRequestData.getSubmittedByUserId());
        payload.put("processInstanceId", execution.getProcessInstanceId());
        // payload.put("taskLink", "url_to_task_if_available"); // Construct if possible

        NotificationRequestDto notificationRequest = NotificationRequestDto.builder()
                .recipientId(recipientId)
                .recipientType(recipientType)
                .channelType(ProcessConstants.NOTIFICATION_CHANNEL_EMAIL) // Or preferred channel
                .templateId(ProcessConstants.TEMPLATE_ID_APPROVAL_REQUEST)
                .payload(payload)
                .build();

        try {
            log.info("Sending approval request notification to {} ({}) for entity {}", recipientId, recipientType, approvalRequestData.getEntityId());
            notificationServiceClient.sendNotification(notificationRequest);
            log.info("Successfully sent approval request notification for process instance {}", execution.getProcessInstanceId());
        } catch (Exception e) {
            log.error("Failed to send approval request notification for process instance {}: {}", execution.getProcessInstanceId(), e.getMessage(), e);
            // Depending on criticality, either log and continue or throw BpmnError
            // throw new BpmnError("NOTIFICATION_SERVICE_FAILED", "Failed to send approval request notification: " + e.getMessage());
        }
    }
}