package com.thesss.platform.farmer.infrastructure.persistence.repository;

import com.thesss.platform.farmer.domain.model.Farmer;
import com.thesss.platform.farmer.domain.model.FarmerId;
import com.thesss.platform.farmer.domain.model.FarmerStatus;
import com.thesss.platform.farmer.domain.repository.FarmerRepository;
import com.thesss.platform.farmer.domain.repository.FarmerSearchCriteria; // Assuming this DTO exists
import com.thesss.platform.farmer.infrastructure.persistence.entity.FarmerJpaEntity;
import com.thesss.platform.farmer.service.mapper.FarmerDtoMapper; // Using general DTO mapper for entity mapping

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import jakarta.persistence.criteria.Predicate; // For JpaSpecificationExecutor
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FarmerRepositoryImpl implements FarmerRepository {

    private final FarmerSpringDataRepository springDataRepository;
    private final FarmerDtoMapper farmerDtoMapper; // Assuming this mapper handles Farmer <-> FarmerJpaEntity

    @Override
    public Farmer save(Farmer farmer) {
        FarmerJpaEntity entity = farmerDtoMapper.toFarmerJpaEntity(farmer);
        // Ensure bidirectional relationships are set correctly if not handled by mapper
        if (entity.getBankAccounts() != null) {
            entity.getBankAccounts().forEach(ba -> ba.setFarmer(entity));
        }
        FarmerJpaEntity savedEntity = springDataRepository.save(entity);
        return farmerDtoMapper.toFarmerDomain(savedEntity);
    }

    @Override
    public Optional<Farmer> findById(FarmerId farmerId) {
        return springDataRepository.findById(farmerId.getValue())
                .map(farmerDtoMapper::toFarmerDomain);
    }

    @Override
    public Optional<Farmer> findByPrimaryPhoneNumber(String phoneNumber) {
        // This specific method is not on FarmerSpringDataRepository by default,
        // so a custom query or using specifications would be needed.
        // For now, let's assume existsByPrimaryPhoneNumberAndStatusIn is the main one for validation.
        // If a full find is needed:
        // return springDataRepository.findByPrimaryPhoneNumber(phoneNumber)
        // .map(farmerDtoMapper::toFarmerDomain);
        // For now, returning empty as it's not explicitly required by the current file list's direct logic.
        Specification<FarmerJpaEntity> spec = (root, query, cb) ->
                cb.equal(root.get("primaryPhoneNumber"), phoneNumber);
        return springDataRepository.findOne(spec).map(farmerDtoMapper::toFarmerDomain);
    }

    @Override
    public boolean existsByPrimaryPhoneNumberAndStatusIn(String phoneNumber, List<FarmerStatus> statuses) {
        List<String> statusStrings = statuses.stream()
                .map(Enum::name)
                .collect(Collectors.toList());
        return springDataRepository.existsByPrimaryPhoneNumberAndStatusIn(phoneNumber, statusStrings);
    }

    @Override
    public Page<Farmer> search(FarmerSearchCriteria criteria, Pageable pageable) {
        Specification<FarmerJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getNamePart() != null && !criteria.getNamePart().isEmpty()) {
                String likePattern = "%" + criteria.getNamePart().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), likePattern),
                        cb.like(cb.lower(root.get("lastName")), likePattern),
                        cb.like(cb.lower(root.get("middleName")), likePattern)
                ));
            }
            if (criteria.getPhoneNumberPart() != null && !criteria.getPhoneNumberPart().isEmpty()) {
                predicates.add(cb.like(root.get("primaryPhoneNumber"), "%" + criteria.getPhoneNumberPart() + "%"));
            }
            if (criteria.getStatusFilter() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatusFilter()));
            }
            // Add more criteria based on FarmerSearchCriteria (e.g., location, etc.)
            // Example for location if homesteadCoordinates are available in criteria
            // if (criteria.getLocationPoint() != null && criteria.getRadiusKm() > 0) {
            //    // This requires PostGIS functions, typically via native query or Hibernate Spatial extensions
            //    // Expression<Point> pointExpression = root.get("homesteadCoordinates");
            //    // Expression<Boolean> distanceFunction = cb.function("ST_DWithin", Boolean.class,
            //    // pointExpression, cb.literal(criteria.getLocationPoint()), cb.literal(criteria.getRadiusKm() * 1000));
            //    // predicates.add(cb.isTrue(distanceFunction));
            // }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<FarmerJpaEntity> entityPage = springDataRepository.findAll(spec, pageable);
        return entityPage.map(farmerDtoMapper::toFarmerDomain);
    }
}