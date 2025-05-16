package com.thesss.platform.farmer.infrastructure.persistence.repository;

import com.thesss.platform.farmer.infrastructure.persistence.entity.FarmerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FarmerSpringDataRepository extends JpaRepository<FarmerJpaEntity, UUID>, JpaSpecificationExecutor<FarmerJpaEntity> {

    /**
     * Checks if a farmer exists with the given primary phone number and one of the specified statuses.
     *
     * @param primaryPhoneNumber The primary phone number to check.
     * @param statuses           A list of farmer status strings (e.g., "ACTIVE", "PENDING_APPROVAL").
     * @return true if such a farmer exists, false otherwise.
     */
    boolean existsByPrimaryPhoneNumberAndStatusIn(String primaryPhoneNumber, List<String> statuses);

    // Example of finding by phone number if needed, though exists check is more common for validation
    // Optional<FarmerJpaEntity> findByPrimaryPhoneNumber(String primaryPhoneNumber);
}