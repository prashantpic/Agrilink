```sql
-- Enable PostGIS extension if not already enabled
CREATE EXTENSION IF NOT EXISTS postgis;

-- Table for Farm Land Record (Aggregate Root)
CREATE TABLE farm_land_record (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL,
    parcel_id VARCHAR(255) NOT NULL,
    parcel_region_code VARCHAR(100) NOT NULL, -- For regional uniqueness of parcel_id
    land_name VARCHAR(255),
    total_area_value DECIMAL(18, 4), -- e.g., 12345.6789
    total_area_unit VARCHAR(50),    -- e.g., 'HA', 'ACRE' (Master Data Code)
    cultivable_area_value DECIMAL(18, 4),
    cultivable_area_unit VARCHAR(50),
    ownership_type VARCHAR(100),    -- Master Data Code
    land_use_category VARCHAR(100), -- Master Data Code
    soil_type VARCHAR(100),         -- Master Data Code
    irrigation_method VARCHAR(100), -- Master Data Code
    topography VARCHAR(100),        -- Master Data Code
    access_method VARCHAR(100),     -- Master Data Code
    elevation_value DECIMAL(10, 2),
    elevation_unit VARCHAR(50),     -- Master Data Code
    status VARCHAR(50) NOT NULL,    -- Enum: DRAFT, PENDING_APPROVAL, ACTIVE, INACTIVE, etc.

    -- Geospatial data (SRID 4326 for WGS84 is common for storage)
    boundary GEOMETRY(GEOMETRY, 4326), -- Can store Polygon or MultiPolygon

    -- Audit information
    created_by VARCHAR(255) NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP WITH TIME ZONE,

    -- Constraint for parcel_id uniqueness within a region
    CONSTRAINT uq_parcel_id_region UNIQUE (parcel_id, parcel_region_code)
);

-- Spatial index for boundary
CREATE INDEX farm_land_record_boundary_idx ON farm_land_record USING GIST (boundary);

-- Index on farmer_id for quick lookups
CREATE INDEX farm_land_record_farmer_id_idx ON farm_land_record (farmer_id);
-- Index on status
CREATE INDEX farm_land_record_status_idx ON farm_land_record (status);


-- Table for Points of Interest (linked to FarmLandRecord)
CREATE TABLE point_of_interest (
    id UUID PRIMARY KEY,
    farm_land_record_id UUID NOT NULL REFERENCES farm_land_record(id) ON DELETE CASCADE,
    location GEOMETRY(Point, 4326) NOT NULL, -- SRID 4326 for WGS84
    name VARCHAR(255) NOT NULL,
    description TEXT,

    -- Audit information (optional for POIs, or inherit from parent)
    created_by VARCHAR(255),
    created_date TIMESTAMP WITH TIME ZONE,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP WITH TIME ZONE
);

-- Spatial index for POI location
CREATE INDEX point_of_interest_location_idx ON point_of_interest USING GIST (location);
-- Index on farm_land_record_id for POIs
CREATE INDEX point_of_interest_farm_land_record_id_idx ON point_of_interest (farm_land_record_id);


-- Table for Lease Details (1-to-1 with FarmLandRecord, or 1-to-many if history is needed)
CREATE TABLE lease_details (
    id UUID PRIMARY KEY,
    farm_land_record_id UUID NOT NULL UNIQUE REFERENCES farm_land_record(id) ON DELETE CASCADE, -- UNIQUE for 1-to-1
    lessor_name VARCHAR(255),
    lessor_contact VARCHAR(255),
    lease_start_date DATE,
    lease_end_date DATE,
    lease_terms TEXT,

    -- Audit information
    created_by VARCHAR(255) NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP WITH TIME ZONE
);

-- Table for Ownership Documents (Many-to-1 with FarmLandRecord)
CREATE TABLE ownership_document (
    id UUID PRIMARY KEY,
    farm_land_record_id UUID NOT NULL REFERENCES farm_land_record(id) ON DELETE CASCADE,
    document_url VARCHAR(1024) NOT NULL, -- URL to the document in object storage
    expiry_date DATE,

    -- Audit information
    created_by VARCHAR(255) NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP WITH TIME ZONE
);
CREATE INDEX ownership_document_farm_land_record_id_idx ON ownership_document (farm_land_record_id);


-- Table for Soil Test History (Many-to-1 with FarmLandRecord)
CREATE TABLE soil_test_history (
    id UUID PRIMARY KEY,
    farm_land_record_id UUID NOT NULL REFERENCES farm_land_record(id) ON DELETE CASCADE,
    test_date DATE NOT NULL,
    ph DECIMAL(4, 2), -- e.g., 6.75
    nitrogen_value DECIMAL(10, 2),
    nitrogen_unit VARCHAR(50),  -- Master Data Code (e.g., KG_PER_HA, PPM)
    phosphorus_value DECIMAL(10, 2),
    phosphorus_unit VARCHAR(50),
    potassium_value DECIMAL(10, 2),
    potassium_unit VARCHAR(50),
    micronutrients TEXT,        -- Free text or JSON/structured data
    test_report_url VARCHAR(1024), -- URL to the report

    -- Audit information
    created_by VARCHAR(255) NOT NULL,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by VARCHAR(255),
    last_modified_date TIMESTAMP WITH TIME ZONE
);
CREATE INDEX soil_test_history_farm_land_record_id_idx ON soil_test_history (farm_land_record_id);


-- Table for Approval History (Many-to-1 with FarmLandRecord)
CREATE TABLE approval_history (
    id UUID PRIMARY KEY,
    farm_land_record_id UUID NOT NULL REFERENCES farm_land_record(id) ON DELETE CASCADE,
    field_changed VARCHAR(255) NOT NULL,
    previous_value TEXT,
    new_value TEXT,
    status VARCHAR(50) NOT NULL, -- Status of this approval entry (e.g., PENDING, APPROVED, REJECTED)
    submitted_by VARCHAR(255) NOT NULL,
    submission_date TIMESTAMP WITH TIME ZONE NOT NULL,
    approved_by VARCHAR(255),
    approval_date TIMESTAMP WITH TIME ZONE,
    comments TEXT,

    -- Audit information for the history entry itself
    created_by_audit VARCHAR(255) NOT NULL,
    created_date_audit TIMESTAMP WITH TIME ZONE NOT NULL,
    last_modified_by_audit VARCHAR(255),
    last_modified_date_audit TIMESTAMP WITH TIME ZONE
);
CREATE INDEX approval_history_farm_land_record_id_idx ON approval_history (farm_land_record_id);

```