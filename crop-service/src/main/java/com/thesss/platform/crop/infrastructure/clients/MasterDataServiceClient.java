package com.thesss.platform.crop.infrastructure.clients;

import com.thesss.platform.crop.application.ports.outgoing.MasterDataServicePort;
import com.thesss.platform.crop.infrastructure.clients.dtos.MasterDataDto; // Assuming this DTO exists
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Optional;

@FeignClient(name = "masterdata-service", url = "${services.masterdata-service.url}")
public interface MasterDataServiceClient extends MasterDataServicePort {

    // Assumes MasterData service has endpoints like:
    // GET /api/v1/masterdata/{categoryKey}/{itemKey}
    // GET /api/v1/masterdata/crops/duration?cropMasterId=X&varietyMasterIdOrText=Y
    // GET /api/v1/masterdata/units/conversion?from=A&to=B&type=T

    @Override
    @GetMapping("/api/v1/masterdata/{categoryKey}/{itemKey}")
    @CircuitBreaker(name = "masterdataService")
    @Retry(name = "masterdataService")
    Optional<MasterDataDto> getMasterDataValue(@PathVariable("categoryKey") String categoryKey,
                                               @PathVariable("itemKey") String itemKey);

    @Override
    default String resolveMasterDataValue(String masterId, String category) {
        // This is a helper, actual implementation might call getMasterDataValue
        // and extract the 'value' field from MasterDataDto
        try {
            return getMasterDataValue(category, masterId)
                    .map(MasterDataDto::getValue) // Assuming MasterDataDto has a getValue() method
                    .orElse(masterId); // Fallback to ID if not found or DTO is different
        } catch (Exception e) {
            return masterId; // Fallback on error
        }
    }

    @Override
    @GetMapping("/api/v1/masterdata/crops/duration") // Example endpoint
    @CircuitBreaker(name = "masterdataService")
    @Retry(name = "masterdataService")
    Optional<Integer> getCropDurationDays(@RequestParam("cropMasterId") String cropMasterId,
                                          @RequestParam(name = "varietyMasterIdOrText", required = false) String varietyMasterIdOrText);

    @Override
    @GetMapping("/api/v1/masterdata/units/conversion") // Example endpoint
    @CircuitBreaker(name = "masterdataService")
    @Retry(name = "masterdataService")
    Optional<BigDecimal> getUnitConversionFactor(@RequestParam("fromUnitMasterId") String fromUnitMasterId,
                                                 @RequestParam("toUnitMasterId") String toUnitMasterId,
                                                 @RequestParam("quantityType") String quantityType);
}