package com.thesss.platform.workflows.listeners;

import com.thesss.platform.workflows.client.identity.IdentityAccessServiceClient;
import com.thesss.platform.workflows.client.notification.NotificationServiceClient;
import com.thesss.platform.workflows.client.notification.dto.NotificationRequestDto;
// Assuming IdentityAccessServiceClient might provide UserDetailsDto
import com.thesss.platform.workflows.client.identity.dto.UserDetailsDto;
import com.thesss.platform.workflows.utils.ProcessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component("userTaskAssignmentListener")
@RequiredArgsConstructor
public class UserTaskAssignmentListener implements TaskListener {

    private final NotificationServiceClient notificationServiceClient;
    private final IdentityAccessServiceClient identityAccessServiceClient; // For dynamic assignment if needed

    @Override
    public void notify(DelegateTask delegateTask) {
        String eventName = delegateTask.getEventName();
        log.info("UserTaskAssignmentListener triggered for task {}, event: {}, processInstanceId: {}",
                delegateTask.getId(), eventName, delegateTask.getProcessInstanceId());

        // Handle on 'create' or 'assignment' events
        if (TaskListener.EVENTNAME_CREATE.equals(eventName) || TaskListener.EVENTNAME_ASSIGNMENT.equals(eventName)) {
            
            // Optional: Dynamic candidate group/user resolution
            // This part could be complex and might be better suited for a dedicated delegate before task creation.
            // However, if it needs to react to task creation itself:
            // String entityType = (String) delegateTask.getVariable("entityType"); // Example variable
            // if (StringUtils.isEmpty(delegateTask.getAssignee()) && delegateTask.getCandidates().isEmpty()) {
            //     log.info("Task {} has no assignee or candidates, attempting dynamic resolution.", delegateTask.getId());
            //     try {
            //          List<UserDetailsDto> candidates = identityAccessServiceClient.findCandidateUsersForTask(entityType, delegateTask.getProcessInstanceId());
            //          if (candidates != null && !candidates.isEmpty()) {
            //              List<String> candidateUserIds = candidates.stream().map(UserDetailsDto::getId).collect(Collectors.toList());
            //              delegateTask.addCandidateUsers(candidateUserIds);
            //              log.info("Dynamically added candidate users {} to task {}", candidateUserIds, delegateTask.getId());
            //          } else {
            //              // delegateTask.addCandidateGroup("default_fallback_group");
            //              log.warn("No dynamic candidates found for task {}.", delegateTask.getId());
            //          }
            //     } catch (Exception e) {
            //         log.error("Error during dynamic candidate resolution for task {}: {}", delegateTask.getId(), e.getMessage());
            //     }
            // }


            // Send notification to assignee or candidate groups/users
            String assignee = delegateTask.getAssignee();
            String recipientId = null;
            String recipientType = null;

            if (StringUtils.hasText(assignee)) {
                recipientId = assignee;
                recipientType = ProcessConstants.RECIPIENT_TYPE_USER;
            } else if (delegateTask.getCandidateGroups() != null && !delegateTask.getCandidateGroups().isEmpty()) {
                // Notify the first candidate group, or all if NotificationService supports it
                recipientId = delegateTask.getCandidateGroups().iterator().next(); // Taking the first one
                recipientType = ProcessConstants.RECIPIENT_TYPE_GROUP;
            } else if (delegateTask.getCandidateUsers() != null && !delegateTask.getCandidateUsers().isEmpty()) {
                 // Notify the first candidate user, or all
                recipientId = delegateTask.getCandidateUsers().iterator().next(); // Taking the first one
                recipientType = ProcessConstants.RECIPIENT_TYPE_USER_LIST_FOR_GROUP_NOTIFICATION_OR_INDIVIDUAL; // Special type or handle individually
            }

            if (recipientId != null) {
                sendNotification(delegateTask, recipientId, recipientType);
            } else {
                log.warn("No assignee or candidate group/users found for task {} to send notification.", delegateTask.getId());
            }
        }
    }

    private void sendNotification(DelegateTask delegateTask, String recipientId, String recipientType) {
        Map<String, String> payload = new HashMap<>();
        payload.put("taskId", delegateTask.getId());
        payload.put("taskName", delegateTask.getName());
        payload.put("processInstanceId", delegateTask.getProcessInstanceId());
        // Add more context from process variables if needed
        Object approvalRequestDataObj = delegateTask.getVariable(ProcessConstants.VAR_APPROVAL_REQUEST_DATA);
        if (approvalRequestDataObj instanceof com.thesss.platform.workflows.model.process.ApprovalRequestData) {
            com.thesss.platform.workflows.model.process.ApprovalRequestData ard = (com.thesss.platform.workflows.model.process.ApprovalRequestData) approvalRequestDataObj;
            payload.put("entityId", ard.getEntityId());
            payload.put("entityType", ard.getEntityType());
        }
        // payload.put("taskLink", "url_to_task_ui/" + delegateTask.getId()); // Construct a deep link to the task

        NotificationRequestDto notificationRequest = NotificationRequestDto.builder()
                .recipientId(recipientId)
                .recipientType(recipientType)
                .channelType(ProcessConstants.NOTIFICATION_CHANNEL_EMAIL) // Default channel
                .templateId(ProcessConstants.TEMPLATE_ID_TASK_ASSIGNMENT) // Specific template for task assignment
                .payload(payload)
                .build();

        try {
            log.info("Sending task assignment notification for task {} to {} ({})", delegateTask.getId(), recipientId, recipientType);
            notificationServiceClient.sendNotification(notificationRequest);
            log.info("Successfully sent task assignment notification for task {}", delegateTask.getId());
        } catch (Exception e) {
            log.error("Failed to send task assignment notification for task {}: {}", delegateTask.getId(), e.getMessage(), e);
            // Non-critical usually, log and continue.
        }
    }
}