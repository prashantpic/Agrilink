package com.thesss.platform.gateway.service;

import com.thesss.platform.gateway.dto.ConsentStatusResponse;
import reactor.core.publisher.Mono;

public interface ConsentServiceClient {

    /**
     * Verifies farmer consent for a specific data scope and third-party client.
     *
     * @param farmerId The unique identifier of the farmer.
     * @param dataScope The scope of data for which consent is being checked (e.g., "personal_info", "farm_details").
     * @param thirdPartyClientId The identifier of the third-party client requesting access.
     * @return A Mono emitting {@link ConsentStatusResponse} indicating whether consent is granted and relevant details.
     */
    Mono<ConsentStatusResponse> verifyConsent(String farmerId, String dataScope, String thirdPartyClientId);
}