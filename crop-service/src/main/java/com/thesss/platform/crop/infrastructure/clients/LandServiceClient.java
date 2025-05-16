package com.thesss.platform.crop.infrastructure.clients;

import com.thesss.platform.crop.application.ports.outgoing.LandServicePort;
import com.thesss.platform.crop.infrastructure.clients.dtos.LandRecordDto; // Assuming this DTO exists
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;
import java.util.UUID;

@FeignClient(name = "land-service", url = "${services.land-service.url}")
public interface LandServiceClient extends LandServicePort {

    @Override
    @GetMapping("/api/v1/land-records/{landRecordId}") // Example endpoint path
    @CircuitBreaker(name = "landService")
    @Retry(name = "landService")
    Optional<LandRecordDto> getLandRecordDetails(@PathVariable("landRecordId") UUID landRecordId);

    @Override
    default boolean validateLandRecordExists(UUID landRecordId) {
        // Similar to FarmerServiceClient, prefer a dedicated exists endpoint if available
        // For now, using getLandRecordDetails
        try {
            return getLandRecordDetails(landRecordId).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    // Example for dedicated exists endpoint:
    // @GetMapping("/api/v1/land-records/{landRecordId}/exists")
    // @CircuitBreaker(name = "landService")
    // @Retry(name = "landService")
    // ResponseEntity<Void> checkLandRecordExistence(@PathVariable("landRecordId") UUID landRecordId);
    //
    // default boolean validateLandRecordExists(UUID landRecordId) {
    // try {
    // ResponseEntity<Void> response = checkLandRecordExistence(landRecordId);
    // return response.getStatusCode().is2xxSuccessful();
    // } catch (Exception e) {
    // return false;
    // }
    // }
}