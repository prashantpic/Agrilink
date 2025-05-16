package com.thesss.platform.workflows.client.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
    private String recipientUserId;       // If targeting a specific user in the system
    private String recipientEmail;        // For email notifications
    private String recipientPhoneNumber;  // For SMS notifications
    private List<String> channels;        // e.g., ["EMAIL", "SMS", "IN_APP"]
    private String templateId;            // ID of a pre-defined notification template
    private Map<String, Object> parameters; // Parameters to fill into the template
    private String subject;               // Required if not using a template that defines it (e.g., for email)
    private String messageContent;        // Raw message content if not using a template
}