package com.thesss.platform.gateway.service;

import com.thesss.platform.gateway.dto.ConsentStatusResponse;
import reactor.core.publisher.Mono;

public interface ConsentServiceClient {

    /**
     * Verifies if a farmer has granted consent for a specific data scope to a third-party client.
     *
     * @param farmerId The unique identifier of the farmer.
     * @param dataScope The specific scope of data access being requested (e.g., "farm-details", "crop-history").
     * @param thirdPartyClientId The unique identifier of the third-party client requesting access.
     * @return A Mono emitting {@link ConsentStatusResponse} indicating whether consent is granted and relevant details.
     */
    Mono<ConsentStatusResponse> verifyConsent(String farmerId, String dataScope, String thirdPartyClientId);
}