-- Read Model for Farmer Land Summary
CREATE TABLE farmer_land_summary_view (
    farmer_id VARCHAR(36) PRIMARY KEY,
    total_land_area DOUBLE PRECISION DEFAULT 0.0,
    number_of_plots INT DEFAULT 0,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_flsv_farmer_id ON farmer_land_summary_view(farmer_id);

-- Read Model for Farmer Active Crop Cycles
CREATE TABLE farmer_active_crop_cycle_view (
    id VARCHAR(36) PRIMARY KEY, -- Can be crop_cycle_id itself if universally unique
    farmer_id VARCHAR(36) NOT NULL,
    crop_cycle_id VARCHAR(36) NOT NULL UNIQUE,
    crop_name VARCHAR(255),
    cultivated_area DOUBLE PRECISION,
    status VARCHAR(50), -- e.g., 'ACTIVE', 'PLANTED', 'GROWING'
    start_date DATE,
    expected_harvest_date DATE,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_faccv_farmer_id ON farmer_active_crop_cycle_view(farmer_id);
CREATE INDEX idx_faccv_crop_cycle_id ON farmer_active_crop_cycle_view(crop_cycle_id);
CREATE INDEX idx_faccv_farmer_id_status ON farmer_active_crop_cycle_view(farmer_id, status);

-- Read Model for Farmer Harvest KPIs
CREATE TABLE farmer_harvest_kpi_view (
    id VARCHAR(36) PRIMARY KEY, -- A new UUID for this record, or use crop_cycle_id if 1:1 and always present
    farmer_id VARCHAR(36) NOT NULL,
    crop_cycle_id VARCHAR(36) NOT NULL UNIQUE,
    crop_name VARCHAR(255),
    cultivated_area DOUBLE PRECISION,
    total_production DOUBLE PRECISION, -- e.g., in kilograms
    average_yield DOUBLE PRECISION, -- e.g., kg per hectare
    harvest_date DATE,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fhkv_farmer_id ON farmer_harvest_kpi_view(farmer_id);
CREATE INDEX idx_fhkv_crop_cycle_id ON farmer_harvest_kpi_view(crop_cycle_id);
CREATE INDEX idx_fhkv_farmer_id_harvest_date ON farmer_harvest_kpi_view(farmer_id, harvest_date DESC);

-- Read Model for System-wide Statistics (Single Row Table)
CREATE TABLE system_stats_view (
    id VARCHAR(50) PRIMARY KEY, -- e.g., 'singleton_stats_id'
    total_registered_farmers BIGINT DEFAULT 0,
    total_managed_land_area DOUBLE PRECISION DEFAULT 0.0,
    total_active_crop_records BIGINT DEFAULT 0, -- Number of active crop cycles
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Initialize the singleton row for system_stats_view
INSERT INTO system_stats_view (id, total_registered_farmers, total_managed_land_area, total_active_crop_records, last_updated_at)
VALUES ('singleton_stats_id', 0, 0.0, 0, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;


-- Read Model for Data Quality Metrics (Single Row Table)
CREATE TABLE data_quality_metrics_view (
    id VARCHAR(50) PRIMARY KEY, -- e.g., 'singleton_metrics_id'
    farmer_profile_completeness_percentage DOUBLE PRECISION DEFAULT 0.0,
    land_gps_coverage_percentage DOUBLE PRECISION DEFAULT 0.0,
    -- Add other data quality metrics as needed
    total_farmers_for_completeness_calc BIGINT DEFAULT 0, -- Denominator for farmer profile completeness
    total_land_plots_for_gps_calc BIGINT DEFAULT 0, -- Denominator for GPS coverage
    land_plots_with_gps_for_gps_calc BIGINT DEFAULT 0, -- Numerator for GPS coverage
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Initialize the singleton row for data_quality_metrics_view
INSERT INTO data_quality_metrics_view (id, farmer_profile_completeness_percentage, land_gps_coverage_percentage, total_farmers_for_completeness_calc, total_land_plots_for_gps_calc, land_plots_with_gps_for_gps_calc, last_updated_at)
VALUES ('singleton_metrics_id', 0.0, 0.0, 0, 0, 0, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Function to update last_updated_at column automatically (Optional, can be handled by application logic)
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply the trigger to tables if you want DB to manage last_updated_at
-- Note: R2DBC/Spring Data might handle this at application level more easily for reactive flows.
-- Example for one table:
-- CREATE TRIGGER update_farmer_land_summary_modtime
-- BEFORE UPDATE ON farmer_land_summary_view
-- FOR EACH ROW EXECUTE FUNCTION update_modified_column();
-- (Repeat for other tables if desired)