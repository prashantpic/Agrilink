import { Database, Model, Q } from '@nozbe/watermelondb';
import ConflictRecord, { ResolutionStatus, ConflictType } from '../data/local/models/ConflictRecord';
import { TableName } from '../data/local/TableName';
// Assuming BaseModel/SyncableModel interface from ConflictDetector.ts
interface SyncableModel extends Model {
  id: string;
  syncStatus: string;
  lastModifiedLocallyAt: number;
  serverLastModifiedAt?: number | null;
  // Writer method to apply updates
  updateRecord: (data: any) => Promise<void>; 
  [key: string]: any; 
}

// Configuration for LWW fields - this should come from a config file or be more dynamic
const LAST_WRITE_WINS_CONFIG: { [tableName: string]: string[] } = {
  // Example: For 'farmers' table, 'notes' and 'secondary_phone' fields can use LWW
  [TableName.FARMERS]: ['notes', 'secondary_phone', 'middle_name', 'email', 'family_size', 'farming_experience_years'],
  [TableName.LAND_RECORDS]: ['parcel_id', 'cultivable_area'],
  [TableName.CROP_CYCLES]: ['notes', 'expected_yield_unit', 'actual_yield_unit'],
  // Define for other tables as needed
};

export enum ManualResolutionStrategy {
  KEEP_LOCAL = 'KEEP_LOCAL',
  TAKE_SERVER = 'TAKE_SERVER',
  MERGE_MANUAL = 'MERGE_MANUAL', // Implies mergedData will be provided
}

export class ConflictResolver {
  private database: Database;

  constructor(database: Database) {
    this.database = database;
  }

  /**
   * Attempts to automatically resolve a conflict.
   * Currently implements Last-Write-Wins (LWW) for configured fields.
   * @param conflictRecord The conflict record to resolve.
   * @returns Promise<boolean> True if auto-resolved, false otherwise.
   */
  public async attemptAutoResolve(conflictRecord: ConflictRecord): Promise<boolean> {
    if (conflictRecord.conflictType !== ConflictType.EDIT_EDIT) {
      // LWW typically applies to EDIT_EDIT conflicts on specific fields.
      // Other types like UNIQUENESS_VIOLATION or EDIT_DELETE usually need manual review.
      return false;
    }

    const lwwFields = LAST_WRITE_WINS_CONFIG[conflictRecord.entityTableName] || [];
    if (lwwFields.length === 0) {
      return false; // No LWW fields configured for this table
    }

    try {
      const localData = JSON.parse(conflictRecord.localData || '{}');
      const serverData = JSON.parse(conflictRecord.serverData || '{}');

      const localTimestamp = localData.lastModifiedLocallyAt || localData.updatedAt || 0; // From the snapshot
      const serverTimestamp = serverData.serverLastModifiedAt || serverData.updated_at || 0; // From the snapshot

      // Basic LWW: if all conflicting fields are in lwwFields list
      // More sophisticated LWW would check field by field.
      // Here, we simplify: if local is newer, local wins for LWW fields, server wins otherwise (or vice-versa)
      // This is a very basic LWW. A proper one would merge non-conflicting fields and apply LWW per field.

      // For simplicity, this example assumes a full "take local" or "take server" for LWW.
      // A more robust LWW would be field-level.
      // SDS: "last-write-wins for designated fields based on timestamps"

      const targetCollection = this.database.collections.get<SyncableModel>(conflictRecord.entityTableName);
      const conflictedEntity = await targetCollection.find(conflictRecord.entityId);

      if (localTimestamp > serverTimestamp) { // Local is newer
        // Apply local changes (they are already there). Mark as resolved and ready for PUSH.
        await this.database.write(async () => {
          await conflictRecord.updateResolution(ResolutionStatus.RESOLVED_AUTO_LWW_LOCAL, 'Auto-resolved: Local version was newer.');
          await conflictedEntity.update(e => {
            e.syncStatus = 'PENDING_UPDATE'; // To be pushed to server
            e.lastModifiedLocallyAt = Date.now(); // Update timestamp after resolution
          });
        });
        console.log(`Conflict ${conflictRecord.id} auto-resolved (LWW): Kept local for ${conflictRecord.entityTableName}/${conflictRecord.entityId}`);
        return true;
      } else if (serverTimestamp > localTimestamp) { // Server is newer
        // Apply server changes to local record.
        await this.database.write(async () => {
            // Create a delta from serverData to apply to conflictedEntity
            // Only update fields that are part of the LWW strategy or general data fields.
            // This is simplified. A real merge would be more careful.
            const updatePayload:any = {};
            for(const key in serverData) {
                if(key !== 'id' && key !== 'remote_id' && key !== 'created_at' && key !== 'updated_at' && key !== 'server_last_modified_at' && key !== 'last_modified_locally_at' && key !== 'sync_status') {
                    if (conflictedEntity._raw.hasOwnProperty(key) ) { // Check if field exists on model
                        updatePayload[key] = serverData[key];
                    }
                }
            }
            await conflictedEntity.update(e => {
                Object.assign(e, updatePayload); // Apply server fields
                e.serverLastModifiedAt = serverTimestamp; // Update with server's timestamp
                e.syncStatus = 'SYNCED'; // Reflects server state now
                e.lastModifiedLocallyAt = Date.now(); // Update timestamp after resolution
            });
            await conflictRecord.updateResolution(ResolutionStatus.RESOLVED_AUTO_LWW_SERVER, 'Auto-resolved: Server version was newer.');
        });
        console.log(`Conflict ${conflictRecord.id} auto-resolved (LWW): Applied server for ${conflictRecord.entityTableName}/${conflictRecord.entityId}`);
        return true;
      } else {
        // Timestamps are equal, or one is missing. Cannot auto-resolve with simple LWW.
        return false;
      }
    } catch (error) {
      console.error(`Error during auto-resolution for conflict ${conflictRecord.id}:`, error);
      return false;
    }
  }

  /**
   * Resolves a conflict based on manual user decision.
   * @param conflictRecordId ID of the ConflictRecord.
   * @param strategy How to resolve (KEEP_LOCAL, TAKE_SERVER, MERGE_MANUAL).
   * @param mergedDataPayload If strategy is MERGE_MANUAL, this is the data to apply (JSON string).
   */
  public async resolveManually(
    conflictRecordId: string,
    strategy: ManualResolutionStrategy,
    mergedDataPayload?: string,
  ): Promise<void> {
    await this.database.write(async () => {
      const conflict = await this.database.collections.get<ConflictRecord>(TableName.CONFLICT_RECORDS).find(conflictRecordId);
      if (!conflict) throw new Error(`ConflictRecord ${conflictRecordId} not found.`);
      if (conflict.resolutionStatus !== ResolutionStatus.PENDING && !conflict.resolutionStatus.startsWith('RESOLVED_AUTO')) { // Allow re-resolution of auto ones
          // console.warn(`Conflict ${conflictRecordId} is not pending, current status: ${conflict.resolutionStatus}`);
          // return; // Or throw error, depending on desired behavior for re-resolving.
      }


      const targetCollection = this.database.collections.get<SyncableModel>(conflict.entityTableName);
      const conflictedEntity = await targetCollection.find(conflict.entityId);

      let resolutionStatus: ResolutionStatus;
      let resolutionNotes = `Manually resolved: ${strategy}.`;

      switch (strategy) {
        case ManualResolutionStrategy.KEEP_LOCAL:
          // Local data is already there. Mark entity for re-sync.
          await conflictedEntity.update(e => {
            e.syncStatus = 'PENDING_UPDATE';
            e.lastModifiedLocallyAt = Date.now(); // Touch the record
          });
          resolutionStatus = ResolutionStatus.RESOLVED_MANUAL_LOCAL;
          break;

        case ManualResolutionStrategy.TAKE_SERVER:
          if (!conflict.serverData) throw new Error('Server data is missing for TAKE_SERVER strategy.');
          const serverData = JSON.parse(conflict.serverData);
          
          const serverUpdatePayload:any = {};
            for(const key in serverData) {
                // Exclude meta fields unless specifically handled
                if(key !== 'id' && key !== 'remote_id' && key !== '_raw' && 
                   !key.startsWith('_') && // WDB internal fields
                   conflictedEntity._raw.hasOwnProperty(key) ) {
                    serverUpdatePayload[key] = serverData[key];
                }
            }
            
          await conflictedEntity.update(e => {
            Object.assign(e, serverUpdatePayload);
            e.serverLastModifiedAt = serverData.serverLastModifiedAt || serverData.updated_at || e.serverLastModifiedAt;
            e.syncStatus = 'PENDING_UPDATE'; // Needs to be pushed as resolved state
            e.lastModifiedLocallyAt = Date.now();
          });
          resolutionStatus = ResolutionStatus.RESOLVED_MANUAL_SERVER;
          break;

        case ManualResolutionStrategy.MERGE_MANUAL:
          if (!mergedDataPayload) throw new Error('Merged data is missing for MERGE_MANUAL strategy.');
          const mergedData = JSON.parse(mergedDataPayload);

          const mergedUpdatePayload:any = {};
            for(const key in mergedData) {
                 if(key !== 'id' && key !== 'remote_id' && key !== '_raw' && 
                   !key.startsWith('_') &&
                   conflictedEntity._raw.hasOwnProperty(key) ) {
                    mergedUpdatePayload[key] = mergedData[key];
                }
            }

          await conflictedEntity.update(e => {
            Object.assign(e, mergedUpdatePayload);
            // serverLastModifiedAt might need to be set from mergedData if it represents a server state primarily
            // For now, we assume merged data is the new "truth" to be pushed.
            e.syncStatus = 'PENDING_UPDATE';
            e.lastModifiedLocallyAt = Date.now();
          });
          resolutionStatus = ResolutionStatus.RESOLVED_MANUAL_MERGED;
          resolutionNotes += ` Data: ${mergedDataPayload}`;
          break;

        default:
          throw new Error(`Unknown resolution strategy: ${strategy}`);
      }

      await conflict.updateResolution(resolutionStatus, resolutionNotes);
      console.log(`Conflict ${conflict.id} resolved manually (${strategy}) for ${conflict.entityTableName}/${conflict.entityId}`);
    });
  }

  /**
   * Marks a conflict as requiring manual resolution, usually if auto-resolve fails or is not applicable.
   * @param conflictRecord The conflict record.
   * @param reason Why it needs manual resolution.
   */
  public async flagForManualResolution(conflictRecord: ConflictRecord, reason: string): Promise<void> {
    await this.database.write(async () => {
        // Check if it's already pending to avoid redundant updates, or if we want to update the reason
        if (conflictRecord.resolutionStatus !== ResolutionStatus.PENDING) {
             await conflictRecord.update(cr => {
                cr.resolutionStatus = ResolutionStatus.PENDING;
                cr.resolutionNotes = (cr.resolutionNotes ? cr.resolutionNotes + '; ' : '') + `Flagged for manual: ${reason}`;
                cr.updatedAt = Date.now();
            });
        } else {
            // Optionally update notes if already pending
             await conflictRecord.update(cr => {
                cr.resolutionNotes = (cr.resolutionNotes ? cr.resolutionNotes + '; ' : '') + `Re-flagged: ${reason}`;
                cr.updatedAt = Date.now();
            });
        }
    });
    console.log(`Conflict ${conflictRecord.id} flagged for manual resolution: ${reason}`);
  }
}