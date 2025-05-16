package com.thesss.platform.farmer.domain.repository;

import com.thesss.platform.farmer.domain.model.Farmer;
import com.thesss.platform.farmer.domain.model.FarmerId;
import com.thesss.platform.farmer.domain.model.FarmerStatus;
// import com.thesss.platform.farmer.service.dto.FarmerSearchCriteria; // Or define a domain search criteria
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Interface for Farmer aggregate persistence operations.
 * REQ-FRM-001, REQ-FRM-006
 */
public interface FarmerRepository {

    Farmer save(Farmer farmer);

    Optional<Farmer> findById(FarmerId farmerId);

    Optional<Farmer> findByPrimaryPhoneNumber(String phoneNumber);

    /**
     * Checks if a farmer exists with the given primary phone number and one of the specified statuses.
     * REQ-FRM-006
     * @param phoneNumber The primary phone number to check.
     * @param statuses A list of {@link FarmerStatus} to filter by.
     * @return true if such a farmer exists, false otherwise.
     */
    boolean existsByPrimaryPhoneNumberAndStatusIn(String phoneNumber, List<FarmerStatus> statuses);

    // Define FarmerSearchCriteria in domain or adapt from service DTO
    // For now, assuming a placeholder or a simple map/object criteria
    Page<Farmer> search(Object criteria, Pageable pageable); // Replace Object with FarmerSearchCriteria

}