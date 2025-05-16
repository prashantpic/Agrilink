package com.thesss.platform.crop.application.ports.outgoing;

import com.thesss.platform.crop.infrastructure.clients.dtos.FarmerDto;
import java.util.Optional;
import java.util.UUID;

public interface FarmerServicePort {

    Optional<FarmerDto> getFarmerDetails(UUID farmerId);

    boolean validateFarmerExists(UUID farmerId);
}