import { appSchema, tableSchema } from '@nozbe/watermelondb/Schema';
import { SyncStatus, ConflictResolutionStatus, ConflictType, SyncOperationType } from '../../../types/sync'; // Assuming types are defined

// REQ-14-001: Defines schema for Farmer, LandRecord, GPSData, CropCycle
// REQ-14-003: Local IDs are UUIDs (handled by model definitions where `id` is primary key, type string)

const baseSchemaColumns = [
  // `id` is implicitly the primary key, defined in models as string (UUID)
  { name: 'remote_id', type: 'string', isOptional: true, isIndexed: true },
  { name: 'sync_status', type: 'string', isIndexed: true }, // e.g., 'PENDING_CREATE', 'SYNCED', 'CONFLICTS_PENDING'
  { name: 'last_modified_locally_at', type: 'number', isIndexed: true },
  { name: 'server_last_modified_at', type: 'number', isOptional: true, isIndexed: true },
  { name: 'created_at', type: 'number' },
  { name: 'updated_at', type: 'number' },
  // { name: 'deleted_at', type: 'number', isOptional: true, isIndexed: true }, // For soft deletes
];

export const localAppSchema = appSchema({
  version: 1, // Increment version for migrations
  tables: [
    tableSchema({
      name: 'farmers',
      columns: [
        ...baseSchemaColumns,
        { name: 'name', type: 'string' },
        { name: 'phone_number', type: 'string', isOptional: true, isIndexed: true }, // Example field requiring uniqueness
        { name: 'id_details', type: 'string', isOptional: true }, // JSON string for complex ID details
        // Add other farmer-specific fields based on REQ-14-001
        { name: 'address', type: 'string', isOptional: true }, // Could be JSON or simple string
      ],
    }),
    tableSchema({
      name: 'land_records',
      columns: [
        ...baseSchemaColumns,
        { name: 'farmer_id', type: 'string', isIndexed: true }, // Foreign key to farmers table
        { name: 'area', type: 'number', isOptional: true },
        { name: 'location_description', type: 'string', isOptional: true },
        // Add other land_record-specific fields based on REQ-14-001
        { name: 'land_parcel_id', type: 'string', isOptional: true, isIndexed: true },
      ],
    }),
    tableSchema({
      name: 'gps_data', // REQ-1.3-001, REQ-1.3-003
      columns: [
        ...baseSchemaColumns,
        { name: 'land_record_id', type: 'string', isIndexed: true }, // Foreign key to land_records table
        { name: 'latitude', type: 'number' },
        { name: 'longitude', type: 'number' },
        { name: 'altitude', type: 'number', isOptional: true },
        { name: 'accuracy', type: 'number', isOptional: true },
        { name: 'timestamp', type: 'number' },
        { name: 'type', type: 'string' }, // 'POINT', 'POLYGON', 'POLYLINE'
        { name: 'data', type: 'string' }, // GeoJSON string for polygon/polyline, or specific point data
      ],
    }),
    tableSchema({
      name: 'crop_cycles',
      columns: [
        ...baseSchemaColumns,
        { name: 'land_record_id', type: 'string', isIndexed: true }, // Foreign key to land_records table
        { name: 'crop_type', type: 'string' },
        { name: 'planting_date', type: 'number', isOptional: true }, // Stored as timestamp
        { name: 'harvest_date', type: 'number', isOptional: true }, // Stored as timestamp
        { name: 'expected_yield', type: 'number', isOptional: true },
        // Add other crop_cycle-specific fields based on REQ-14-001
      ],
    }),
    tableSchema({
      name: 'sync_queue_items', // REQ-14-002 (implicitly, as this tracks operations)
      columns: [
        // `id` will be the primary key (UUID)
        { name: 'entity_type', type: 'string', isIndexed: true }, // e.g., 'Farmer', 'LandRecord'
        { name: 'entity_id', type: 'string', isIndexed: true }, // Local UUID of the record
        { name: 'operation_type', type: 'string' }, // 'CREATE', 'UPDATE', 'DELETE' (enum: SyncOperationType)
        { name: 'payload', type: 'string' }, // JSON string of the data to sync for CREATE/UPDATE
        { name: 'attempt_count', type: 'number', isOptional: true },
        { name: 'last_attempt_at', type: 'number', isOptional: true },
        { name: 'error_message', type: 'string', isOptional: true },
        { name: 'created_at', type: 'number' },
      ],
    }),
    tableSchema({
      name: 'conflict_records', // REQ-14-004
      columns: [
        // `id` will be the primary key (UUID)
        { name: 'entity_type', type: 'string', isIndexed: true },
        { name: 'entity_id', type: 'string', isIndexed: true }, // Local UUID of the conflicted record
        { name: 'local_data', type: 'string' }, // JSON string of local version
        { name: 'server_data', type: 'string', isOptional: true }, // JSON string of server version
        { name: 'conflict_type', type: 'string' }, // 'EDIT_EDIT', 'EDIT_DELETE', 'UNIQUENESS_VIOLATION' (enum: ConflictType)
        { name: 'resolution_status', type: 'string', isIndexed: true }, // 'PENDING', 'RESOLVED_LOCAL', 'RESOLVED_SERVER', 'RESOLVED_MERGED' (enum: ConflictResolutionStatus)
        { name: 'resolved_at', type: 'number', isOptional: true },
        { name: 'resolution_details', type: 'string', isOptional: true }, // e.g., notes from manual resolution
        { name: 'created_at', type: 'number' },
        { name: 'updated_at', type: 'number' },
      ],
    }),
  ],
});

export default localAppSchema;