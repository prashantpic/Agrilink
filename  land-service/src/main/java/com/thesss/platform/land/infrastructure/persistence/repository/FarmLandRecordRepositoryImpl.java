package com.thesss.platform.land.infrastructure.persistence.repository;

import com.thesss.platform.land.application.port.out.FarmLandRecordRepositoryPort;
import com.thesss.platform.land.domain.model.FarmLandRecord;
import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;
import com.thesss.platform.land.infrastructure.persistence.entity.FarmLandRecordJpaEntity;
import com.thesss.platform.land.infrastructure.persistence.mapper.FarmLandRecordPersistenceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class FarmLandRecordRepositoryImpl implements FarmLandRecordRepositoryPort {

    private final SpringDataFarmLandRecordRepository springDataRepository;
    private final FarmLandRecordPersistenceMapper persistenceMapper;

    @Autowired
    public FarmLandRecordRepositoryImpl(
            SpringDataFarmLandRecordRepository springDataRepository,
            FarmLandRecordPersistenceMapper persistenceMapper) {
        this.springDataRepository = springDataRepository;
        this.persistenceMapper = persistenceMapper;
    }

    @Override
    @Transactional
    public FarmLandRecord save(FarmLandRecord farmLandRecord) {
        FarmLandRecordJpaEntity jpaEntity;
        if (farmLandRecord.getId() == null || !springDataRepository.existsById(farmLandRecord.getId().getValue())) {
            jpaEntity = persistenceMapper.toJpaEntity(farmLandRecord);
            if (jpaEntity.getId() == null) { // Ensure ID is set if domain didn't generate one
                jpaEntity.setId(UUID.randomUUID()); // Or use a sequence from DB
            }
            jpaEntity.isNew(true); // Mark as new for Persistable
        } else {
            FarmLandRecordJpaEntity existingEntity = springDataRepository.findById(farmLandRecord.getId().getValue())
                    .orElseThrow(() -> new IllegalStateException("FarmLandRecord not found for update: " + farmLandRecord.getId().getValue()));
            jpaEntity = persistenceMapper.updateJpaEntity(farmLandRecord, existingEntity);
            jpaEntity.isNew(false);
        }

        FarmLandRecordJpaEntity savedEntity = springDataRepository.save(jpaEntity);
        return persistenceMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FarmLandRecord> findById(LandRecordId id) {
        Optional<FarmLandRecordJpaEntity> jpaEntityOptional = springDataRepository.findById(id.getValue());
        return jpaEntityOptional.map(persistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FarmLandRecord> findAll(Pageable pageable) {
        Page<FarmLandRecordJpaEntity> jpaEntityPage = springDataRepository.findAll(pageable);
        List<FarmLandRecord> domainContent = jpaEntityPage.getContent().stream()
                .map(persistenceMapper::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(domainContent, pageable, jpaEntityPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FarmLandRecord> findByFarmerId(FarmerId farmerId, Pageable pageable) {
        Page<FarmLandRecordJpaEntity> jpaEntityPage = springDataRepository.findByFarmerId(farmerId.getValue(), pageable);
        List<FarmLandRecord> domainContent = jpaEntityPage.getContent().stream()
                .map(persistenceMapper::toDomain)
                .collect(Collectors.toList());
        return new PageImpl<>(domainContent, pageable, jpaEntityPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByParcelIdAndRegion(String parcelId, String region) {
        return springDataRepository.existsByParcelIdAndParcelRegionCode(parcelId, region);
    }
}