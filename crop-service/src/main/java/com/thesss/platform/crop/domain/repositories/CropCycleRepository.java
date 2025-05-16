package com.thesss.platform.crop.domain.repositories;

import com.thesss.platform.crop.domain.model.CropCycle;
import com.thesss.platform.crop.domain.model.CropCycleId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CropCycleRepository {

    CropCycle save(CropCycle cropCycle);

    Optional<CropCycle> findById(UUID id);

    Optional<CropCycle> findByCropCycleId(CropCycleId cropCycleId);

    List<CropCycle> findByFarmerId(UUID farmerId);

    List<CropCycle> findByLandRecordId(UUID landRecordId);

    // void delete(CropCycle cropCycle); // Standard delete can be part of a generic repository or Spring Data
}