// Assuming syncConstants.ts defines an enum or object for status strings
// import { SyncStatusConstant } from '../core/constants/syncConstants'; // Example import

// Placeholder for SyncStatusConstant - replace with actual import and definition
export enum SyncStatusConstant {
  IDLE = 'IDLE',
  SYNCING = 'SYNCING',
  SYNCED = 'SYNCED',
  SYNC_FAILED = 'SYNC_FAILED',
  OFFLINE = 'OFFLINE',
  CONFLICTS_PENDING = 'CONFLICTS_PENDING',
  REQUIRES_UNIQUENESS_VALIDATION = 'REQUIRES_UNIQUENESS_VALIDATION',
  PENDING_CREATE = 'PENDING_CREATE',
  PENDING_UPDATE = 'PENDING_UPDATE',
  PENDING_DELETE = 'PENDING_DELETE',
}
// End placeholder

/**
 * Represents the possible synchronization statuses of a local record or the sync system.
 * REQ-14-008: Used by syncStatusManager and UI components.
 */
export type SyncStatus = SyncStatusConstant;


/**
 * Defines the type of operation for a sync queue item.
 */
export enum SyncOperationType {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
}

/**
 * Represents the payload for a sync queue item.
 * Typically the data of the entity being synced.
 */
export type SyncQueueItemPayload = Record<string, any>;


/**
 * Status of a conflict resolution.
 * REQ-14-004, REQ-14-010
 */
export enum ConflictResolutionStatus {
  PENDING = 'PENDING', // Conflict detected, awaiting resolution
  RESOLVED_LOCAL = 'RESOLVED_LOCAL', // Resolved by choosing local version
  RESOLVED_SERVER = 'RESOLVED_SERVER', // Resolved by choosing server version
  RESOLVED_MERGED = 'RESOLVED_MERGED', // Resolved by merging data (manual or automatic)
  // RESOLVED_AUTO_LWW = 'RESOLVED_AUTO_LWW', // Auto-resolved by Last-Write-Wins
}

/**
 * Type of data conflict detected.
 * REQ-14-004
 */
export enum ConflictType {
  EDIT_EDIT = 'EDIT_EDIT', // Both local and server versions were modified
  EDIT_DELETE = 'EDIT_DELETE', // Local was edited, server record was deleted
  DELETE_EDIT = 'DELETE_EDIT', // Local was deleted, server record was edited/updated
  UNIQUENESS_VIOLATION = 'UNIQUENESS_VIOLATION', // A unique constraint was violated on the server
  // Other types can be added as needed
}

/**
 * Detailed structure of a detected data conflict.
 * This structure will be stored in the `ConflictRecord` table.
 * REQ-14-004
 */
export interface ConflictDetail {
  id: string; // Local UUID of the ConflictRecord itself
  entity_type: string; // e.g., 'Farmer', 'LandRecord'
  entity_id: string; // Local UUID of the conflicted record in its own table
  
  local_data: Record<string, any> | null; // Snapshot of local record data at time of conflict
  server_data: Record<string, any> | null; // Snapshot of server record data causing conflict
  
  conflict_type: ConflictType;
  resolution_status: ConflictResolutionStatus;
  
  message?: string; // Optional message, e.g., for uniqueness violation details
  
  resolved_by_user_id?: string; // UUID of user who resolved it (if manual)
  resolved_at?: number; // Timestamp of resolution
  
  created_at: number; // Timestamp when conflict was recorded
  updated_at: number; // Timestamp of last update to this conflict record
}

/**
 * Represents the choices a user can make during manual conflict resolution.
 * REQ-14-010
 */
export enum ManualConflictResolutionStrategy {
  KEEP_LOCAL = 'KEEP_LOCAL',
  TAKE_SERVER = 'TAKE_SERVER',
  MERGE_MANUALLY = 'MERGE_MANUALLY', // Implies user provides merged data
}

/**
 * Payload for a synchronization operation (pushing changes to server).
 */
export interface SyncOperationPayload {
  type: SyncOperationType;
  entity: string; // e.g., 'farmers', 'land_records'
  localId: string; // Local UUID
  remoteId?: string | null; // Server's ID for the record, if known
  data?: Record<string, any>; // For CREATE and UPDATE operations
  timestamp: number; // last_modified_locally_at
}

/**
 * Structure for changes received from the server.
 */
export interface RemoteChangeItem {
  remote_id: string;
  entity_type: string; // e.g., 'Farmer'
  data: Record<string, any> | null; // Null if deleted
  server_last_modified_at: number;
  is_deleted?: boolean;
}