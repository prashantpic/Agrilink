-- Flyway migration script for creating initial read model tables

-- Farmer Land Summary View
-- Stores aggregated land information per farmer.
CREATE TABLE farmer_land_summary_view (
    farmer_id VARCHAR(36) PRIMARY KEY,          -- Assuming UUID for farmerId
    total_land_area DOUBLE PRECISION DEFAULT 0.0,
    number_of_plots INT DEFAULT 0,
    last_updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_flsv_farmer_id ON farmer_land_summary_view(farmer_id);

-- Farmer Active Crop Cycle View
-- Stores information about currently active crop cycles for farmers.
CREATE TABLE farmer_active_crop_cycle_view (
    id VARCHAR(36) PRIMARY KEY,                  -- Assuming UUID for the view entry itself
    farmer_id VARCHAR(36) NOT NULL,
    crop_cycle_id VARCHAR(36) NOT NULL UNIQUE,   -- Assuming UUID for cropCycleId
    crop_name VARCHAR(255),
    cultivated_area DOUBLE PRECISION,
    status VARCHAR(50),                          -- e.g., 'ACTIVE', 'PLANTED', 'GROWING'
    start_date DATE,
    last_updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_faccv_farmer_id ON farmer_active_crop_cycle_view(farmer_id);
CREATE INDEX idx_faccv_crop_cycle_id ON farmer_active_crop_cycle_view(crop_cycle_id);
CREATE INDEX idx_faccv_farmer_id_status ON farmer_active_crop_cycle_view(farmer_id, status);

-- Farmer Harvest KPI View
-- Stores key performance indicators related to harvests per crop cycle.
CREATE TABLE farmer_harvest_kpi_view (
    id VARCHAR(36) PRIMARY KEY,                  -- Assuming UUID for the view entry
    farmer_id VARCHAR(36) NOT NULL,
    crop_cycle_id VARCHAR(36) NOT NULL UNIQUE,
    crop_name VARCHAR(255),
    average_yield DOUBLE PRECISION,              -- e.g., yield per hectare
    total_production DOUBLE PRECISION,
    harvest_date DATE,
    last_updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_fhkv_farmer_id ON farmer_harvest_kpi_view(farmer_id);
CREATE INDEX idx_fhkv_crop_cycle_id ON farmer_harvest_kpi_view(crop_cycle_id);
CREATE INDEX idx_fhkv_farmer_id_harvest_date ON farmer_harvest_kpi_view(farmer_id, harvest_date DESC);

-- System Stats View
-- A single-row table storing system-wide statistics.
CREATE TABLE system_stats_view (
    id VARCHAR(50) PRIMARY KEY DEFAULT 'singleton', -- Fixed ID for the single row
    total_registered_farmers BIGINT DEFAULT 0,
    total_managed_land_area DOUBLE PRECISION DEFAULT 0.0,
    total_crop_records BIGINT DEFAULT 0,            -- Could be total active crop cycles or all historical
    last_updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
-- Ensure only one row can exist if 'singleton' ID is used (application logic should enforce this)
-- Or use a check constraint if DB supports it well for this case.
-- INSERT INTO system_stats_view (id) VALUES ('singleton') ON CONFLICT (id) DO NOTHING; (handled by application logic on startup or first event)


-- Data Quality Metrics View
-- A single-row table storing system-wide data quality metrics.
CREATE TABLE data_quality_metrics_view (
    id VARCHAR(50) PRIMARY KEY DEFAULT 'singleton', -- Fixed ID for the single row
    farmer_profile_completeness_percentage DOUBLE PRECISION DEFAULT 0.0,
    land_gps_coverage_percentage DOUBLE PRECISION DEFAULT 0.0,
    -- Add other DQ metrics as needed
    last_updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);
-- Ensure only one row can exist
-- INSERT INTO data_quality_metrics_view (id) VALUES ('singleton') ON CONFLICT (id) DO NOTHING; (handled by application logic)