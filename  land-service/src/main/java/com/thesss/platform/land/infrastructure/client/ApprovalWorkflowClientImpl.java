package com.thesss.platform.land.infrastructure.client;

import com.thesss.platform.land.application.port.out.ApprovalWorkflowServicePort;
import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;
import com.thesss.platform.land.exception.ExternalServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApprovalWorkflowClientImpl implements ApprovalWorkflowServicePort {

    private static final Logger logger = LoggerFactory.getLogger(ApprovalWorkflowClientImpl.class);
    private final RestTemplate restTemplate;
    private final String approvalServiceBaseUrl;

    private static final String APPROVAL_WORKFLOW_SERVICE_CB = "approvalWorkflowService";

    // Define DTOs for request and response if the external API uses them.
    // For simplicity, using Map here.
    private static class ApprovalRequest {
        public String landRecordId;
        public String farmerId;
        public String changeSummary;
        // Add other fields as required by the Approval Workflow service
    }

    private static class ApprovalResponse {
        public String workflowInstanceId;
        // Add other fields as returned by the Approval Workflow service
    }


    @Autowired
    public ApprovalWorkflowClientImpl(RestTemplate restTemplate,
                                      @Value("${app.land-service.external-service-endpoints.approval-workflow-service-base-url}") String approvalServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.approvalServiceBaseUrl = approvalServiceBaseUrl;
    }

    @Override
    @CircuitBreaker(name = APPROVAL_WORKFLOW_SERVICE_CB, fallbackMethod = "initiateLandRecordApprovalFallback")
    public String initiateLandRecordApproval(LandRecordId landRecordId, FarmerId farmerId, String changeSummary) {
        String url = String.format("%s/api/v1/workflows/land-record/initiate", approvalServiceBaseUrl);
        logger.debug("Initiating land record approval: URL='{}', LandRecordId='{}', FarmerId='{}', Summary='{}'",
                url, landRecordId.getValue(), farmerId.getValue(), changeSummary);

        ApprovalRequest requestPayload = new ApprovalRequest();
        requestPayload.landRecordId = landRecordId.getValue().toString();
        requestPayload.farmerId = farmerId.getValue().toString();
        requestPayload.changeSummary = changeSummary;

        HttpEntity<ApprovalRequest> requestEntity = new HttpEntity<>(requestPayload);

        try {
            ResponseEntity<ApprovalResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, ApprovalResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().workflowInstanceId != null) {
                logger.info("Approval workflow initiated successfully for LandRecordId='{}', WorkflowInstanceId='{}'",
                        landRecordId.getValue(), response.getBody().workflowInstanceId);
                return response.getBody().workflowInstanceId;
            }
            logger.warn("Unexpected response status {} from ApprovalWorkflowService for LandRecordId='{}'",
                    response.getStatusCode(), landRecordId.getValue());
            throw new ExternalServiceException("Approval workflow initiation failed with status: " + response.getStatusCode());
        } catch (HttpStatusCodeException e) {
            logger.error("HttpStatusCodeException while initiating approval workflow: LandRecordId='{}', status={}, body={}",
                    landRecordId.getValue(), e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new ExternalServiceException("Error initiating approval workflow for land record " + landRecordId.getValue() + ". Status: " + e.getStatusCode(), e);
        } catch (Exception e) {
            logger.error("Exception while initiating approval workflow: LandRecordId='{}'", landRecordId.getValue(), e);
            throw new ExternalServiceException("Error initiating approval workflow for land record " + landRecordId.getValue(), e);
        }
    }

    public String initiateLandRecordApprovalFallback(LandRecordId landRecordId, FarmerId farmerId, String changeSummary, Throwable t) {
        logger.warn("ApprovalWorkflowService.initiateLandRecordApproval fallback triggered for LandRecordId='{}'. Error: {}",
                landRecordId.getValue(), t.getMessage());
        // Critical operation: re-throw to signal failure.
        throw new ExternalServiceException("Approval workflow service unavailable. Failed to initiate workflow for land record " + landRecordId.getValue(), t);
    }
}