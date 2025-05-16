import apiClient, { ApiClient } from '../data/remote/apiClient';
import { Database } from '@nozbe/watermelondb';
import { BaseModel } from '../data/local/models/BaseModel';
import { ConflictRecord } from '../data/local/models/ConflictRecord'; // Assuming this model exists
import { SyncStatus } from '../constants/syncConstants';
import { ConflictType } from '../types/sync'; // Define this type

/**
 * REQ-14-005: Detection and resolution of data conflicts (uniqueness validation part).
 * REQ-14-010: UI for manual conflict resolution (flagging for this UI).
 */
export class UniquenessValidator {
  private apiClient: ApiClient;
  private database: Database;

  constructor(apiClientInstance: ApiClient, databaseInstance: Database) {
    this.apiClient = apiClientInstance;
    this.database = databaseInstance;
  }

  /**
   * Checks uniqueness of a field value on the server.
   * @param record The local WatermelonDB record instance.
   * @param entityType The type of the entity (e.g., 'farmers').
   * @param fieldName The name of the field to check (e.g., 'primaryPhone').
   * @param value The value of the field to check.
   * @returns True if unique, false otherwise.
   */
  async checkUniqueness(
    record: BaseModel,
    entityType: string,
    fieldName: string,
    value: any,
  ): Promise<boolean> {
    try {
      // Endpoint e.g., /validation/uniqueness/{entityType}/{fieldName}?value={value}&currentRecordRemoteId={remoteId}
      // currentRecordRemoteId is important for updates, to exclude the current record itself from the check.
      const params: any = { value };
      if (record.remoteId) {
        params.currentRecordRemoteId = record.remoteId;
      } else {
        params.currentRecordLocalId = record.id; // For new records, server might need localId to exclude if it already processed it partially
      }

      const response = await this.apiClient.get(
        `/validate-uniqueness/${entityType}/${fieldName}`,
        { params },
      );

      if (response.data.isUnique) {
        return true;
      } else {
        // Uniqueness violation
        console.warn(
          `UniquenessValidator: Field '${fieldName}' with value '${value}' for ${entityType} ID '${record.id}' is not unique on server. Conflicting ID: ${response.data.conflictingRecordId}`,
        );
        
        await this.database.write(async () => {
          await record.setRequiresUniquenessValidation();

          const conflictRecordsCollection = this.database.get<ConflictRecord>('conflict_records');
          // Check if a conflict record already exists for this entity and uniqueness type
          const existingConflicts = await conflictRecordsCollection.query(
            Q.where('entity_id', record.id),
            Q.where('conflict_type', ConflictType.UNIQUENESS_VIOLATION)
            // Potentially add fieldName to conflict_type or a new field in ConflictRecord
          ).fetch();


          const serverConflictData = {
            message: `Value '${value}' for field '${fieldName}' already exists.`,
            conflictingRemoteId: response.data.conflictingRecordId,
            // Server might provide more details about the conflicting record
            ...(response.data.conflictingRecordSnapshot || {}), 
          };

          if (existingConflicts.length > 0) {
            await existingConflicts[0].update(cr => {
              cr.localData = record._raw; // Freshest local data
              cr.serverData = serverConflictData;
              cr.resolvedAt = null;
              cr.resolutionStatus = 'PENDING';
            });
          } else {
            await conflictRecordsCollection.create(cr => {
              cr.entityId = record.id;
              cr.entityType = entityType; // Model's table name
              cr.localData = record._raw; 
              cr.serverData = serverConflictData;
              cr.conflictType = ConflictType.UNIQUENESS_VIOLATION;
              cr.resolutionStatus = 'PENDING';
              // cr.fieldName = fieldName; // Optional: add fieldName to ConflictRecord model
            });
          }
        });
        return false;
      }
    } catch (error) {
      console.error(
        `UniquenessValidator: Error validating uniqueness for ${entityType}.${fieldName} = ${value}`,
        error,
      );
      // Depending on error type, might let it pass or fail validation
      // For now, assume network or server error means we can't confirm, so treat as potentially not unique to be safe, or retry later.
      // Let's assume for now it should not block the flow, but log it. Sync engine might retry.
      // If we return false, it flags for manual resolution.
      // If server is down, we shouldn't flag everything. This needs a strategy.
      // For now, let's assume an error means it couldn't be validated, so it's 'uncertain', not strictly 'not unique'.
      // Perhaps the SyncEngine should handle this error type specifically.
      // For now, return true to not block, but log. This might not be ideal for strict uniqueness.
      // Alternative: throw error and let SyncEngine decide.
      // Let's be conservative: if validation API fails, we cannot guarantee uniqueness.
      // So, mark for attention.
      await this.database.write(async () => {
        await record.update(r => {
            // A different status could be used for "validation_failed_due_to_error"
            // For now, using REQUIRES_UNIQUENESS_VALIDATION
            r.syncStatus = SyncStatus.REQUIRES_UNIQUENESS_VALIDATION; 
        });
      });
      return false; // Indicate validation could not be confirmed positively.
    }
  }
}

// Need to import Q for query
import { Q } from '@nozbe/watermelondb';