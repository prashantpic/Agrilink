package com.thesss.platform.crop.infrastructure.clients;

import com.thesss.platform.crop.application.ports.outgoing.FarmerServicePort;
import com.thesss.platform.crop.infrastructure.clients.dtos.FarmerDto; // Assuming this DTO exists
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity; // For boolean validation

import java.util.Optional;
import java.util.UUID;

@FeignClient(name = "farmer-service", url = "${services.farmer-service.url}")
public interface FarmerServiceClient extends FarmerServicePort {

    @Override
    @GetMapping("/api/v1/farmers/{farmerId}")
    @CircuitBreaker(name = "farmerService")
    @Retry(name = "farmerService")
    Optional<FarmerDto> getFarmerDetails(@PathVariable("farmerId") UUID farmerId);

    @Override
    default boolean validateFarmerExists(UUID farmerId) {
        // Option 1: Use a dedicated endpoint if available (more efficient)
        // return checkFarmerExistence(farmerId);

        // Option 2: Use getFarmerDetails and check if present (less efficient but works if no dedicated endpoint)
        try {
            return getFarmerDetails(farmerId).isPresent();
        } catch (Exception e) {
            // Log error, Feign exceptions might indicate service down or 404
            // Depending on strictness, can return false or rethrow a specific application exception
            return false;
        }
    }

    // Example of a dedicated existence check endpoint (if Farmer Service provides it)
    // @GetMapping("/api/v1/farmers/{farmerId}/exists")
    // @CircuitBreaker(name = "farmerService")
    // @Retry(name = "farmerService")
    // ResponseEntity<Void> checkFarmerExistence(@PathVariable("farmerId") UUID farmerId);
    //
    // default boolean validateFarmerExists(UUID farmerId) {
    //     try {
    //         ResponseEntity<Void> response = checkFarmerExistence(farmerId);
    //         return response.getStatusCode().is2xxSuccessful();
    //     } catch (Exception e) {
    //         // Handle FeignException (e.g., 404 means not exists, other errors mean service issue)
    //         return false;
    //     }
    // }
}