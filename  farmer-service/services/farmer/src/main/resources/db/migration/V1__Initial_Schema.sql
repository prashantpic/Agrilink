-- REQ-FRM-002, REQ-FRM-003, REQ-FRM-005, REQ-FRM-006, REQ-FRM-007, REQ-FRM-008, REQ-FRM-009
-- REQ-FRM-010, REQ-FRM-011, REQ-FRM-012, REQ-FRM-013, REQ-FRM-014, REQ-FRM-015
-- REQ-FRM-016, REQ-FRM-017, REQ-FRM-018, REQ-FRM-019, REQ-FRM-020, REQ-FRM-021, REQ-FRM-022

-- Enable PostGIS extension if not already enabled
CREATE EXTENSION IF NOT EXISTS postgis;

-- Farmer Status Enum Type (Optional, can also be VARCHAR check constraint)
-- CREATE TYPE farmer_status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'APPROVED', 'SUSPENDED', 'DECEASED');
-- Gender Enum Type (Optional)
-- CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT_TO_SAY');

CREATE TABLE farmers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(50) NOT NULL, -- Maps to Gender enum/master data; using VARCHAR for flexibility initially

    primary_phone_number VARCHAR(20) NOT NULL,
    secondary_phone_number VARCHAR(20),
    email_address VARCHAR(100),

    -- Address (Embedded)
    street_address_village VARCHAR(255),
    tehsil_taluk_block VARCHAR(100),
    district VARCHAR(100),
    state_province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),

    -- Coordinates (PostGIS Point)
    homestead_coordinates GEOMETRY(Point, 4326), -- SRID 4326 for WGS 84

    -- National ID (Encrypted)
    national_id_type VARCHAR(50),
    encrypted_national_id_number VARCHAR(255), -- Stores Base64 encoded encrypted value

    family_size INTEGER,
    years_of_farming_experience INTEGER,
    education_level VARCHAR(100), -- Linked to master data
    preferred_language VARCHAR(50), -- Linked to master data

    status VARCHAR(50) NOT NULL, -- Maps to FarmerStatus enum
    date_of_status_change TIMESTAMP WITH TIME ZONE,
    reason_for_status_change TEXT,

    profile_photo_url VARCHAR(512),

    -- Audit Info
    date_of_registration TIMESTAMP WITH TIME ZONE NOT NULL,
    registered_by VARCHAR(100) NOT NULL,
    last_updated_date TIMESTAMP WITH TIME ZONE NOT NULL,
    last_updated_by VARCHAR(100) NOT NULL
);

-- Indexes for farmers table
CREATE INDEX idx_farmers_primary_phone_number ON farmers (primary_phone_number);
CREATE INDEX idx_farmers_status ON farmers (status);
CREATE INDEX idx_farmers_last_name_first_name ON farmers (last_name, first_name);
CREATE INDEX idx_farmers_homestead_coordinates ON farmers USING GIST (homestead_coordinates); -- Spatial index

-- REQ-FRM-006: Unique primary phone number for ACTIVE or PENDING_APPROVAL farmers
-- This can be complex to enforce with a simple UNIQUE INDEX for multiple statuses if NULLs are involved with other statuses.
-- A partial index is the best DB-level approach.
-- Application-level check is crucial as a fallback or primary mechanism.
CREATE UNIQUE INDEX uq_farmers_primary_phone_active_pending
ON farmers (primary_phone_number)
WHERE status IN ('ACTIVE', 'PENDING_APPROVAL');


-- Bank Accounts Table (One-to-Many relationship with Farmer)
-- REQ-FRM-011
CREATE TABLE farmer_bank_accounts (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    account_holder_name VARCHAR(150) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    encrypted_account_number VARCHAR(255) NOT NULL, -- Stores Base64 encoded encrypted value
    branch_name VARCHAR(100),
    ifsc_swift_code VARCHAR(20),
    purpose_of_bank_details VARCHAR(255)
);

CREATE INDEX idx_farmer_bank_accounts_farmer_id ON farmer_bank_accounts (farmer_id);

-- Memberships Table (Element Collection or One-to-Many)
-- REQ-FRM-013, REQ-FRM-014
CREATE TABLE farmer_memberships (
    id UUID PRIMARY KEY, -- Or use farmer_id and organization_name as composite PK if truly an element collection
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    organization_name VARCHAR(150) NOT NULL,
    membership_id VARCHAR(50)
    -- If treating as a simple list of VOs and JPA @ElementCollection maps it to its own table,
    -- then a separate ID might not be needed, farmer_id + org_name could be PK.
    -- For simplicity with potential for expansion, giving it an ID.
);
-- If not using UUID above, then:
-- CREATE TABLE farmer_memberships (
--     farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
--     organization_name VARCHAR(150) NOT NULL,
--     membership_id VARCHAR(50),
--     PRIMARY KEY (farmer_id, organization_name) -- Example if it's a set of VOs
-- );

CREATE INDEX idx_farmer_memberships_farmer_id ON farmer_memberships (farmer_id);

-- Consents Table (Element Collection or One-to-Many)
-- REQ-FRM-021
CREATE TABLE farmer_consents (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    consent_given BOOLEAN NOT NULL,
    consent_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    consent_purpose TEXT NOT NULL,
    consent_version_id VARCHAR(50) NOT NULL
);
-- Or if using @ElementCollection and want a composite primary key:
-- CREATE TABLE farmer_consents (
--     farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
--     consent_purpose TEXT NOT NULL,
--     consent_version_id VARCHAR(50) NOT NULL,
--     consent_given BOOLEAN NOT NULL,
--     consent_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
--     PRIMARY KEY (farmer_id, consent_purpose, consent_version_id)
-- );

CREATE INDEX idx_farmer_consents_farmer_id ON farmer_consents (farmer_id);

-- Approval History Table (Element Collection or One-to-Many)
-- REQ-FRM-022
CREATE TABLE farmer_approval_history (
    id UUID PRIMARY KEY,
    farmer_id UUID NOT NULL REFERENCES farmers(id) ON DELETE CASCADE,
    field_name_changed VARCHAR(100),
    previous_value TEXT,
    new_value TEXT,
    submitted_by_user_id VARCHAR(100) NOT NULL,
    submission_date TIMESTAMP WITH TIME ZONE NOT NULL,
    approval_status VARCHAR(50) NOT NULL, -- e.g., PENDING, APPROVED, REJECTED, BYPASSED_ADMIN
    approved_rejected_by_user_id VARCHAR(100),
    approval_rejection_date TIMESTAMP WITH TIME ZONE,
    approver_comments TEXT
);

CREATE INDEX idx_farmer_approval_history_farmer_id ON farmer_approval_history (farmer_id);
CREATE INDEX idx_farmer_approval_history_submission_date ON farmer_approval_history (submission_date);