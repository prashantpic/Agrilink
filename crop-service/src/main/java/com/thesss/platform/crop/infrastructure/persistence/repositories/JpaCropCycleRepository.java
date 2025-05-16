package com.thesss.platform.crop.infrastructure.persistence.repositories;

import com.thesss.platform.crop.domain.model.CropCycle;
import com.thesss.platform.crop.domain.model.CropCycleId; // Used for query methods
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaCropCycleRepository extends JpaRepository<CropCycle, UUID> {

    // Method to find by the embedded CropCycleId's 'value' field
    Optional<CropCycle> findByCropCycleId_Value(UUID cropCycleIdValue);

    // Alternative using @Query, can be more explicit
    @Query("SELECT cc FROM CropCycle cc WHERE cc.cropCycleId.value = :cropCycleBusinessId")
    Optional<CropCycle> findByCropCycleBusinessId(@Param("cropCycleBusinessId") UUID cropCycleBusinessId);

    List<CropCycle> findByFarmerId(UUID farmerId);

    List<CropCycle> findByLandRecordId(UUID landRecordId);

    // JpaRepository already provides:
    // <S extends CropCycle> S save(S entity);
    // Optional<CropCycle> findById(UUID id);
    // void delete(CropCycle entity);
    // ... and other standard methods.
}