package com.thesss.platform.land.infrastructure.client;

import com.thesss.platform.land.application.port.out.MasterDataServicePort;
import com.thesss.platform.land.exception.ExternalServiceException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class MasterDataClientImpl implements MasterDataServicePort {

    private static final Logger logger = LoggerFactory.getLogger(MasterDataClientImpl.class);
    private final RestTemplate restTemplate;
    private final String masterDataServiceBaseUrl;

    private static final String MASTER_DATA_SERVICE_CB = "masterDataService";

    @Autowired
    public MasterDataClientImpl(RestTemplate restTemplate,
                                @Value("${app.land-service.external-service-endpoints.master-data-service-base-url}") String masterDataServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.masterDataServiceBaseUrl = masterDataServiceBaseUrl;
    }

    @Override
    @CircuitBreaker(name = MASTER_DATA_SERVICE_CB, fallbackMethod = "isValidCodeFallback")
    public boolean isValidCode(String type, String code) {
        String url = String.format("%s/api/v1/masterdata/codes/%s/%s/validate", masterDataServiceBaseUrl, type, code);
        logger.debug("Validating master data code: URL='{}'", url);
        try {
            ResponseEntity<Map<String, Boolean>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                boolean isValid = response.getBody().getOrDefault("valid", false);
                logger.debug("Validation result for type='{}', code='{}': {}", type, code, isValid);
                return isValid;
            }
            logger.warn("Unexpected response status {} from MasterDataService for code validation: type='{}', code='{}'", response.getStatusCode(), type, code);
            return false;
        } catch (HttpStatusCodeException e) {
            logger.error("HttpStatusCodeException while validating master data code: type='{}', code='{}', status={}, body={}", type, code, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new ExternalServiceException("Error validating master data code: " + type + "/" + code + ". Status: " + e.getStatusCode(), e);
        } catch (Exception e) {
            logger.error("Exception while validating master data code: type='{}', code='{}'", type, code, e);
            throw new ExternalServiceException("Error validating master data code: " + type + "/" + code, e);
        }
    }

    public boolean isValidCodeFallback(String type, String code, Throwable t) {
        logger.warn("MasterDataService.isValidCode fallback triggered for type='{}', code='{}'. Error: {}", type, code, t.getMessage());
        // Fallback to false (assume invalid) for safety
        return false;
    }

    // Example getUnitList method
    // @Override
    // @CircuitBreaker(name = MASTER_DATA_SERVICE_CB, fallbackMethod = "getUnitListFallback")
    // public List<String> getUnitList(String type) {
    //     String url = String.format("%s/api/v1/masterdata/units/%s", masterDataServiceBaseUrl, type);
    //     logger.debug("Fetching unit list for type: URL='{}'", url);
    //     try {
    //         ResponseEntity<List<String>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
    //         if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
    //             logger.debug("Fetched unit list for type='{}': {}", type, response.getBody());
    //             return response.getBody();
    //         }
    //         logger.warn("Unexpected response status {} from MasterDataService for unit list: type='{}'", response.getStatusCode(), type);
    //         return Collections.emptyList();
    //     } catch (HttpStatusCodeException e) {
    //         logger.error("HttpStatusCodeException while fetching unit list: type='{}', status={}, body={}", type, e.getStatusCode(), e.getResponseBodyAsString(), e);
    //         throw new ExternalServiceException("Error fetching unit list for type: " + type + ". Status: " + e.getStatusCode(), e);
    //     } catch (Exception e) {
    //         logger.error("Exception while fetching unit list: type='{}'", type, e);
    //         throw new ExternalServiceException("Error fetching unit list for type: " + type, e);
    //     }
    // }

    // public List<String> getUnitListFallback(String type, Throwable t) {
    //     logger.warn("MasterDataService.getUnitList fallback triggered for type='{}'. Error: {}", type, t.getMessage());
    //     return Collections.emptyList();
    // }
}