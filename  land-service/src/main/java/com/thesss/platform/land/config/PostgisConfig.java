```java
package com.thesss.platform.land.config;

import com.fasterxml.jackson.databind.Module;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.n52.jackson.datatype.jts.JtsModule; // Jackson JTS Module
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // REQ-2-020, REQ-1.3-012
public class PostgisConfig {

    // Default SRID for storage, can be overridden by application.yml
    @Value("${app.land-service.geospatial-settings.default-srid-storage:4326}")
    private int defaultSridStorage;

    /**
     * Provides a JTS GeometryFactory bean.
     * The GeometryFactory is used to create JTS Geometry objects.
     * Configured with a floating precision model and the default storage SRID.
     *
     * @return A configured GeometryFactory.
     */
    @Bean // REQ-2-020, REQ-1.3-012
    public GeometryFactory geometryFactory() {
        // PrecisionModel.FLOATING is generally suitable for WGS84 (lat/lon) data.
        // The SRID specifies the coordinate system (e.g., 4326 for WGS84).
        return new GeometryFactory(new PrecisionModel(), defaultSridStorage);
    }

    /**
     * Provides a Jackson Module for JTS (Java Topology Suite) types.
     * This module enables Jackson ObjectMapper to serialize and deserialize JTS Geometry objects
     * to/from GeoJSON, which is useful for API layers or if GeoJSON is stored directly.
     *
     * @return A JtsModule for Jackson.
     */
    @Bean // REQ-2-020
    public Module jtsModule() {
        // This module will be automatically registered with the primary ObjectMapper
        // if it's on the classpath and this bean is defined.
        return new JtsModule();
    }

    // Additional PostGIS or Hibernate Spatial specific configurations could be added here
    // if the auto-configuration is not sufficient. For example, custom Hibernate UserTypes
    // (though hibernate-spatial often provides these).
}
```