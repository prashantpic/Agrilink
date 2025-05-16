package com.thesss.platform.workflows.service;

import com.thesss.platform.workflows.api.dto.CompleteTaskRequestDto;
import com.thesss.platform.workflows.api.dto.TaskDto;
import com.thesss.platform.workflows.exception.WorkflowException;
import com.thesss.platform.workflows.model.process.ApprovalResultData;
import com.thesss.platform.workflows.utils.ProcessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskService {

    private final TaskService taskService;

    public List<TaskDto> getTasksForUser(String userId, String candidateGroup) {
        TaskQuery query = taskService.createTaskQuery().active();

        if (StringUtils.hasText(userId) && StringUtils.hasText(candidateGroup)) {
            query.or()
                 .taskAssignee(userId)
                 .taskCandidateUser(userId)
                 .taskCandidateGroup(candidateGroup)
                 .endOr();
        } else if (StringUtils.hasText(userId)) {
            query.or()
                 .taskAssignee(userId)
                 .taskCandidateUser(userId)
                 .endOr();
        } else if (StringUtils.hasText(candidateGroup)) {
            query.taskCandidateGroup(candidateGroup);
        } else {
            // Potentially return empty or throw error if neither is provided and that's not allowed
            log.warn("Querying tasks without userId or candidateGroup. This might return many tasks.");
        }
        query.orderByTaskCreateTime().desc();

        return query.list().stream()
                .map(this::mapToTaskDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void claimTask(String taskId, String userId) {
        log.info("User {} attempting to claim task {}", userId, taskId);
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new WorkflowException("Task not found with id: " + taskId);
        }
        // Additional checks: task is not already claimed, user is eligible
        if (task.getAssignee() != null && !task.getAssignee().equals(userId)) {
             throw new WorkflowException("Task " + taskId + " is already assigned to " + task.getAssignee());
        }
        taskService.claim(taskId, userId);
        log.info("Task {} claimed by user {}", taskId, userId);
    }

    @Transactional
    public void unclaimTask(String taskId) {
        log.info("Attempting to unclaim task {}", taskId);
         Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new WorkflowException("Task not found with id: " + taskId);
        }
        // Here, we might want to check if the user unclaiming is the current assignee
        // For simplicity, direct unclaim is used.
        taskService.unclaim(taskId);
        log.info("Task {} unclaimed", taskId);
    }

    @Transactional
    public void completeTask(String taskId, CompleteTaskRequestDto requestDto) {
        log.info("Attempting to complete task {} with approved: {}", taskId, requestDto.isApproved());
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new WorkflowException("Task not found with id: " + taskId);
        }

        Map<String, Object> variables = new HashMap<>();
        ApprovalResultData approvalResultData = ApprovalResultData.builder()
                .approved(requestDto.isApproved())
                .approverUserId(requestDto.getApproverUserId()) // This should be the ID of the user completing the task
                .approvalDate(OffsetDateTime.now())
                .comments(requestDto.getComments())
                .build();
        
        variables.put(ProcessConstants.VAR_APPROVAL_RESULT_DATA, approvalResultData);
        // Also directly set individual variables for easier access in BPMN gateways if needed
        variables.put(ProcessConstants.VAR_IS_APPROVED, requestDto.isApproved());
        if (requestDto.getVariables() != null) {
            variables.putAll(requestDto.getVariables());
        }

        taskService.complete(taskId, variables);
        log.info("Task {} completed", taskId);
    }

    private TaskDto mapToTaskDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .assignee(task.getAssignee())
                .createdDate(task.getCreateTime() != null ? task.getCreateTime().toInstant().atOffset(OffsetDateTime.now().getOffset()) : null)
                .dueDate(task.getDueDate() != null ? task.getDueDate().toInstant().atOffset(OffsetDateTime.now().getOffset()) : null)
                .description(task.getDescription())
                .processInstanceId(task.getProcessInstanceId())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .priority(task.getPriority())
                .formKey(task.getFormKey())
                .build();
    }
}