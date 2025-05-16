package com.thesss.platform.workflows.client.land;

import com.thesss.platform.workflows.client.common.ApprovalHistoryEntryDto;
import com.thesss.platform.workflows.client.land.dto.UpdateLandRecordStatusRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "land-service", path = "/api/internal/land-records")
public interface LandServiceClient {

    @PutMapping("/{landRecordId}/status")
    ResponseEntity<Void> updateLandRecordStatus(
            @PathVariable("landRecordId") String landRecordId,
            @RequestBody UpdateLandRecordStatusRequestDto request);

    @PostMapping("/{landRecordId}/approval-history")
    ResponseEntity<Void> addApprovalHistory(
            @PathVariable("landRecordId") String landRecordId,
            @RequestBody ApprovalHistoryEntryDto historyEntry);
}