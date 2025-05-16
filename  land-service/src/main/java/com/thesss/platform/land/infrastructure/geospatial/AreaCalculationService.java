package com.thesss.platform.land.infrastructure.geospatial;

import com.thesss.platform.land.config.AppProperties;
import com.thesss.platform.land.domain.model.Area;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AreaCalculationService {

    private final AppProperties appProperties;
    private static final BigDecimal SQUARE_METERS_TO_HECTARES = new BigDecimal("10000");

    @Autowired
    public AreaCalculationService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public Area calculateArea(Geometry geometry) {
        if (geometry == null || geometry.isEmpty() || geometry.getSRID() == 0) {
            throw new IllegalArgumentException("Geometry must not be null, empty, and must have an SRID set.");
        }
        if (!(geometry instanceof org.locationtech.jts.geom.Polygon || geometry instanceof org.locationtech.jts.geom.MultiPolygon)) {
            throw new IllegalArgumentException("Area calculation is supported only for Polygon or MultiPolygon geometry.");
        }

        double areaInSquareMeters;
        int storageSrid = geometry.getSRID();
        // Ensure defaultSridCalculation has "EPSG:" prefix if not already present
        String calculationSridCode = appProperties.getGeospatialSettings().getDefaultSridCalculation();
        if (!calculationSridCode.toUpperCase().startsWith("EPSG:")) {
            calculationSridCode = "EPSG:" + calculationSridCode;
        }


        try {
            CoordinateReferenceSystem sourceCrs = CRS.decode("EPSG:" + storageSrid);
            CoordinateReferenceSystem targetCrs = CRS.decode(calculationSridCode);

            if (!CRS.equalsIgnoreMetadata(sourceCrs, targetCrs)) { // Use equalsIgnoreMetadata for robust comparison
                MathTransform transform = CRS.findMathTransform(sourceCrs, targetCrs, true); // Lenient true
                Geometry projectedGeometry = JTS.transform(geometry, transform);
                areaInSquareMeters = projectedGeometry.getArea();
            } else {
                areaInSquareMeters = geometry.getArea();
            }
        } catch (FactoryException | TransformException e) {
            throw new RuntimeException("Failed to reproject geometry for area calculation from SRID " + storageSrid + " to " + calculationSridCode, e);
        }

        BigDecimal areaInHectaresBd = BigDecimal.valueOf(areaInSquareMeters)
                .divide(SQUARE_METERS_TO_HECTARES, 4, RoundingMode.HALF_UP); // 4 decimal places for hectares

        return new Area(areaInHectaresBd.doubleValue(), "HA"); // Assuming HA is the target unit code
    }

    public boolean checkAreaDiscrepancy(Area reportedArea, Area calculatedArea) {
        if (reportedArea == null || calculatedArea == null || reportedArea.getValue() == null || calculatedArea.getValue() == null) {
            return false;
        }

        // Assuming both areas are in the same unit (e.g., Hectares) after calculateArea
        // If units can differ, conversion logic is needed here.
        if (!reportedArea.getUnit().equalsIgnoreCase(calculatedArea.getUnit())) {
            // This case needs a unit conversion service. For now, assume they are comparable or same.
            // Or throw an error if units are not the same and conversion is not implemented.
            // For simplicity, we proceed if calculateArea already standardizes to HA
        }

        double reportedValue = reportedArea.getValue();
        double calculatedValue = calculatedArea.getValue();
        double thresholdPercentage = appProperties.getGeospatialSettings().getAreaDiscrepancyThresholdPercentage();

        if (Math.abs(calculatedValue) < 1e-9 && Math.abs(reportedValue) < 1e-9) { // Both effectively zero
            return false;
        }
        if (Math.abs(calculatedValue) < 1e-9) { // Calculated is zero, but reported is not
            return Math.abs(reportedValue) > 1e-9;
        }

        double discrepancy = Math.abs(reportedValue - calculatedValue);
        double percentageDiscrepancy = (discrepancy / calculatedValue) * 100.0;

        return percentageDiscrepancy > thresholdPercentage;
    }
}