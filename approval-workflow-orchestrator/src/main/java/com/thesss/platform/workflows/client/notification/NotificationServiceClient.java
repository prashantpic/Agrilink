package com.thesss.platform.workflows.client.notification;

import com.thesss.platform.workflows.client.notification.dto.NotificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/internal/notifications")
public interface NotificationServiceClient {

    @PostMapping("/send")
    ResponseEntity<Void> sendNotification(
            @RequestBody NotificationRequestDto request);
}