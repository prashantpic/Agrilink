package com.thesss.platform.farmer.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing GPS coordinates.
 * REQ-FRM-009
 */
public final class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCALE = 7; // Standard precision for GPS coordinates

    private final BigDecimal latitude;
    private final BigDecimal longitude;

    public Coordinates(BigDecimal latitude, BigDecimal longitude) {
        Objects.requireNonNull(latitude, "Latitude cannot be null");
        Objects.requireNonNull(longitude, "Longitude cannot be null");

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees.");
        }
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees.");
        }

        this.latitude = latitude.setScale(SCALE, RoundingMode.HALF_UP);
        this.longitude = longitude.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public Coordinates(double latitude, double longitude) {
        this(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
    }


    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        // BigDecimal.equals also checks scale, compareTo ignores scale.
        // For coordinates, value comparison is usually sufficient.
        return latitude.compareTo(that.latitude) == 0 &&
               longitude.compareTo(that.longitude) == 0;
    }

    @Override
    public int hashCode() {
        // Use a consistent representation for hashing
        return Objects.hash(latitude.stripTrailingZeros(), longitude.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return "Coordinates{" +
               "latitude=" + latitude +
               ", longitude=" + longitude +
               '}';
    }
}