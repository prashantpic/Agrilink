package com.thesss.platform.gateway.dto;

import java.time.OffsetDateTime;

/**
 * DTO for consent status verification results.
 * Transports the result of a consent status check from the Consent Service.
 *
 * @param consentGranted        Indicates if consent is granted.
 * @param farmerId              The ID of the farmer.
 * @param dataScope             The scope of data for which consent was checked.
 * @param thirdPartyClientId    The ID of the third-party client requesting access.
 * @param consentTimestamp      The timestamp when the consent was last updated or granted.
 * @param consentVersion        The version of the consent.
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