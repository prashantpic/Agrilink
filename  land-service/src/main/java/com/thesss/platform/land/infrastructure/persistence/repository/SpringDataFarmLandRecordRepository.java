package com.thesss.platform.land.infrastructure.persistence.repository;

import com.thesss.platform.land.infrastructure.persistence.entity.FarmLandRecordJpaEntity;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataFarmLandRecordRepository extends JpaRepository<FarmLandRecordJpaEntity, UUID>, JpaSpecificationExecutor<FarmLandRecordJpaEntity> {

    Page<FarmLandRecordJpaEntity> findByFarmerId(UUID farmerId, Pageable pageable);

    boolean existsByParcelIdAndParcelRegionCode(String parcelId, String parcelRegionCode);

    @Query(value = "SELECT flr.* FROM farm_land_record flr WHERE ST_Intersects(flr.boundary, ST_SetSRID(ST_GeomFromText(:geometryWkt), :srid)) = true",
           nativeQuery = true)
    Page<FarmLandRecordJpaEntity> findByBoundaryIntersectsWkt(@Param("geometryWkt") String geometryWkt, @Param("srid") int srid, Pageable pageable);

    @Query(value = "SELECT flr.* FROM farm_land_record flr WHERE ST_Within(flr.boundary, ST_SetSRID(ST_GeomFromText(:geometryWkt), :srid)) = true",
           nativeQuery = true)
    Page<FarmLandRecordJpaEntity> findByBoundaryWithinWkt(@Param("geometryWkt") String geometryWkt, @Param("srid") int srid, Pageable pageable);

    @Query(value = "SELECT flr.* FROM farm_land_record flr WHERE ST_Contains(flr.boundary, ST_SetSRID(ST_GeomFromText(:geometryWkt), :srid)) = true",
           nativeQuery = true)
    Page<FarmLandRecordJpaEntity> findByBoundaryContainsWkt(@Param("geometryWkt") String geometryWkt, @Param("srid") int srid, Pageable pageable);

    // If passing JTS Geometry objects directly (requires hibernate-spatial or similar properly configured)
    // Ensure the :geometry parameter is correctly handled by Hibernate for native queries.
    // It's often safer to pass WKT/GeoJSON and convert in the query.
    @Query(value = "SELECT flr.* FROM farm_land_record flr WHERE ST_Intersects(flr.boundary, :geometry) = true",
           nativeQuery = true)
    Page<FarmLandRecordJpaEntity> findByBoundaryIntersects(@Param("geometry") Geometry geometry, Pageable pageable);
}