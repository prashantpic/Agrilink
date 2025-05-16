import { Model } from '@nozbe/watermelondb';
import { field, text, readonly, date, writer } from '@nozbe/watermelondb/decorators';
import { TableName } from '../TableName';
// import { generateUUID } from '../../utils/uuidGenerator'; // Assuming a UUID generator utility

export enum ConflictType {
  EDIT_EDIT = 'EDIT_EDIT', // Both local and server made changes to the same record
  EDIT_DELETE = 'EDIT_DELETE', // Local edited, server deleted
  DELETE_EDIT = 'DELETE_EDIT', // Local deleted, server edited
  UNIQUENESS_VIOLATION = 'UNIQUENESS_VIOLATION', // Server reported a unique constraint violation
  // Add other types as needed
}

export enum ResolutionStatus {
  PENDING = 'PENDING', // Awaiting resolution
  RESOLVED_AUTO_LWW_LOCAL = 'RESOLVED_AUTO_LWW_LOCAL', // Auto-resolved: Local version won (Last Write Wins)
  RESOLVED_AUTO_LWW_SERVER = 'RESOLVED_AUTO_LWW_SERVER', // Auto-resolved: Server version won (Last Write Wins)
  RESOLVED_MANUAL_LOCAL = 'RESOLVED_MANUAL_LOCAL', // Manually resolved: Kept local version
  RESOLVED_MANUAL_SERVER = 'RESOLVED_MANUAL_SERVER', // Manually resolved: Took server version
  RESOLVED_MANUAL_MERGED = 'RESOLVED_MANUAL_MERGED', // Manually resolved: Merged data
  // Add other statuses as needed
}

export default class ConflictRecord extends Model {
  static table = TableName.CONFLICT_RECORDS;

  @readonly @text('id') id!: string; // Local UUID for the conflict record itself

  @text('entity_table_name') entityTableName!: string; // e.g., 'farmers', 'land_records'
  @text('entity_id') entityId!: string; // Local UUID of the conflicted data record

  @text('local_data') localData?: string | null; // JSON string snapshot of the local version at time of conflict
  @text('server_data') serverData?: string | null; // JSON string snapshot of the server version at time of conflict

  @text('conflict_type') conflictType!: ConflictType;
  @text('resolution_status') resolutionStatus!: ResolutionStatus;

  @text('resolution_notes') resolutionNotes?: string | null; // Optional notes by admin/resolver
  @date('resolved_at') resolvedAt?: number | null; // Timestamp when resolved

  @readonly @date('created_at') createdAt!: number; // Timestamp when conflict was recorded
  @date('updated_at') updatedAt!: number;


  @writer async createConflictRecord(data: {
    // id: string; // Auto-generated
    entityTableName: string;
    entityId: string;
    localData?: string | null;
    serverData?: string | null;
    conflictType: ConflictType;
  }) {
    const now = Date.now();
    return this.collection.create<ConflictRecord>(record => {
    //   record.id = data.id;
      record.entityTableName = data.entityTableName;
      record.entityId = data.entityId;
      record.localData = data.localData ?? null;
      record.serverData = data.serverData ?? null;
      record.conflictType = data.conflictType;
      record.resolutionStatus = ResolutionStatus.PENDING;
      record.createdAt = now;
      record.updatedAt = now;
    });
  }

  @writer async updateResolution(status: ResolutionStatus, notes?: string) {
    return this.update(record => {
      record.resolutionStatus = status;
      record.resolvedAt = Date.now();
      record.updatedAt = Date.now();
      if (notes) {
        record.resolutionNotes = notes;
      }
    });
  }

  // ConflictRecords are typically deleted (or archived) once resolved and synced.
  @writer async deleteConflictRecord() {
    return super.destroyPermanently();
  }
}