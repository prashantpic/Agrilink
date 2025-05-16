package com.thesss.platform.gateway.dto;

import java.time.OffsetDateTime;

/**
 * DTO for consent status verification results.
 * Transports the result of a consent status check from the Consent Service.
 */
public record ConsentStatusResponse(
    boolean consentGranted,
    String farmerId,
    String dataScope,
    String thirdPartyClientId,
    OffsetDateTime consentTimestamp,
    String consentVersion
) {
}