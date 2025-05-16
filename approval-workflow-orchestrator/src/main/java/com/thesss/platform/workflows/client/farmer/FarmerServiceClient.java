package com.thesss.platform.workflows.client.farmer;

import com.thesss.platform.workflows.client.common.ApprovalHistoryEntryDto;
import com.thesss.platform.workflows.client.farmer.dto.UpdateFarmerStatusRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "farmer-service", path = "/api/internal/farmers")
public interface FarmerServiceClient {

    @PutMapping("/{farmerId}/status")
    ResponseEntity<Void> updateFarmerStatus(
            @PathVariable("farmerId") String farmerId,
            @RequestBody UpdateFarmerStatusRequestDto request);

    @PostMapping("/{farmerId}/approval-history")
    ResponseEntity<Void> addApprovalHistory(
            @PathVariable("farmerId") String farmerId,
            @RequestBody ApprovalHistoryEntryDto historyEntry);
}