package com.thesss.platform.crop.application.ports.outgoing;

import com.thesss.platform.crop.infrastructure.clients.dtos.LandRecordDto;
import java.util.Optional;
import java.util.UUID;

public interface LandServicePort {

    Optional<LandRecordDto> getLandRecordDetails(UUID landRecordId);

    boolean validateLandRecordExists(UUID landRecordId);
}