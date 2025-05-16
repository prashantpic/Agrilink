/**
 * REQ-14-008: Display of synchronization status and history.
 * These constants represent the various states the synchronization process can be in.
 */

export enum SyncStatus {
  IDLE = 'IDLE',
  SYNCING = 'SYNCING',
  SYNCED = 'SYNCED', // Successfully synced
  SYNC_FAILED = 'SYNC_FAILED', // Sync attempt failed
  CONFLICTS_PENDING = 'CONFLICTS_PENDING', // Conflicts detected, manual resolution required
  REQUIRES_UNIQUENESS_VALIDATION = 'REQUIRES_UNIQUENESS_VALIDATION', // Record needs server-side uniqueness check
  OFFLINE = 'OFFLINE', // Device is offline
  INITIAL_SYNC_REQUIRED = 'INITIAL_SYNC_REQUIRED', // App needs to perform an initial sync
  LOCAL_CHANGES_PENDING = 'LOCAL_CHANGES_PENDING', // There are local changes not yet pushed
}

export enum SyncOperationType {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
}

export const SYNC_EVENT_STATUS_CHANGED = 'syncStatusChanged';
export const SYNC_EVENT_PROGRESS_UPDATED = 'syncProgressUpdated';
export const SYNC_EVENT_ERROR = 'syncErrorOccurred';

// Could also define constants for conflict types if they are managed client-side primarily
export enum ConflictType {
  EDIT_EDIT = 'EDIT_EDIT',
  EDIT_DELETE = 'EDIT_DELETE',
  DELETE_EDIT = 'DELETE_EDIT',
  UNIQUENESS_VIOLATION = 'UNIQUENESS_VIOLATION',
}

export enum ResolutionStatus {
  PENDING = 'PENDING',
  RESOLVED_LOCAL = 'RESOLVED_LOCAL',
  RESOLVED_SERVER = 'RESOLVED_SERVER',
  RESOLVED_MERGED = 'RESOLVED_MERGED',
  RESOLVED_MANUAL_EDIT = 'RESOLVED_MANUAL_EDIT',
}