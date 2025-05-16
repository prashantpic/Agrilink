package com.thesss.platform.land.infrastructure.geospatial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Geometry;
import org.n52.jackson.datatype.jts.JtsModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GeoJsonConverter {

    private final ObjectMapper objectMapper;
    private final int defaultSridStorage;


    @Autowired
    public GeoJsonConverter(ObjectMapper objectMapper,
                            com.thesss.platform.land.config.AppProperties appProperties) {
        // Ensure the ObjectMapper has the JTS module registered.
        // This can be done globally in PostgisConfig or locally here.
        // If ObjectMapper bean from Spring context already has it, this might be redundant.
        if (objectMapper.getRegisteredModuleIds().stream().noneMatch(id -> id.equals(JtsModule.class.getName()))) {
             this.objectMapper = objectMapper.copy().registerModule(new JtsModule());
        } else {
            this.objectMapper = objectMapper;
        }
        this.defaultSridStorage = appProperties.getGeospatialSettings().getDefaultSridStorage();
    }

    public String toGeoJson(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(geometry);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JTS Geometry to GeoJSON", e);
        }
    }

    public Geometry fromGeoJson(String geoJson) {
        if (geoJson == null || geoJson.trim().isEmpty()) {
            return null;
        }
        try {
            Geometry geometry = objectMapper.readValue(geoJson, Geometry.class);
            // Ensure SRID is set, Jackson JTS Module may not preserve/set it by default from GeoJSON
            // Standard GeoJSON doesn't typically include SRID in coordinates, but in a 'crs' member.
            // If the input GeoJSON doesn't specify SRID, we assume the default storage SRID.
            if (geometry != null && geometry.getSRID() == 0) {
                geometry.setSRID(this.defaultSridStorage);
            }
            return geometry;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert GeoJSON string to JTS Geometry: " + e.getMessage(), e);
        }
    }
}