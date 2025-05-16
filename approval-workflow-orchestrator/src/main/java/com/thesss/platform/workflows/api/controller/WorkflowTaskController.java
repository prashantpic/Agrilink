package com.thesss.platform.workflows.api.controller;

import com.thesss.platform.workflows.api.dto.CompleteTaskRequestDto;
import com.thesss.platform.workflows.api.dto.TaskDto;
import com.thesss.platform.workflows.service.UserTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows/tasks")
@RequiredArgsConstructor
public class WorkflowTaskController {

    private final UserTaskService userTaskService;

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasksForUser(
            @RequestParam String userId,
            @RequestParam(required = false) String candidateGroup) {
        List<TaskDto> tasks = userTaskService.getTasks(userId, candidateGroup);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{taskId}/claim")
    public ResponseEntity<Void> claimTask(
            @PathVariable String taskId,
            @RequestParam String userId) {
        userTaskService.claimTask(taskId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/unclaim")
    public ResponseEntity<Void> unclaimTask(
            @PathVariable String taskId) {
        userTaskService.unclaimTask(taskId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(
            @PathVariable String taskId,
            @RequestBody @Valid CompleteTaskRequestDto requestDto) {
        userTaskService.completeTask(taskId, requestDto);
        return ResponseEntity.ok().build();
    }
}