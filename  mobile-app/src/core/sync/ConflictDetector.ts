import { Database, Model } from '@nozbe/watermelondb';
import ConflictRecord, { ConflictType } from '../data/local/models/ConflictRecord';
import { TableName } from '../data/local/TableName';
// Assuming BaseModel has these properties, or they are directly on the model
interface SyncableModel extends Model {
  id: string;
  remoteId?: string | null;
  syncStatus: string;
  lastModifiedLocallyAt: number;
  serverLastModifiedAt?: number | null;
  // Other common fields used for serialization if needed
  [key: string]: any; 
}

// This is a simplified representation of what remote data might look like.
// Adjust based on your actual API response structure.
interface RemoteRecordData {
  id: string; // This would be the remote_id from the server
  updated_at: string | number; // ISO string or timestamp from server for last modification
  // ...other fields
  _deleted?: boolean; // Convention for deleted records from server
}

export class ConflictDetector {
  private database: Database;

  constructor(database: Database) {
    this.database = database;
  }

  /**
   * Detects conflict between a local record and incoming server data during a pull operation.
   * @param localRecord The existing local record.
   * @param remoteData The data for this record received from the server.
   * @param lastSuccessfulSyncTimestamp Timestamp of the last successful full sync.
   *                                    Used to determine if both local and remote changed since then.
   * @returns A promise that resolves to a ConflictRecord instance if a conflict is detected, otherwise null.
   */
  public async detectConflictOnPull(
    localRecord: SyncableModel,
    remoteData: RemoteRecordData,
    lastSuccessfulSyncTimestamp: number,
  ): Promise<ConflictRecord | null> {
    const serverModifiedAt = typeof remoteData.updated_at === 'string' 
      ? new Date(remoteData.updated_at).getTime() 
      : remoteData.updated_at;

    const localChangedSinceLastSync = localRecord.lastModifiedLocallyAt > lastSuccessfulSyncTimestamp && 
                                      (localRecord.syncStatus.startsWith('PENDING_') || localRecord.syncStatus === 'CONFLICT');
                                      
    const serverChangedSinceLastSync = serverModifiedAt > lastSuccessfulSyncTimestamp;
    
    let conflictType: ConflictType | null = null;

    if (remoteData._deleted === true) {
      if (localChangedSinceLastSync) {
        // Local was modified, but server says it's deleted.
        conflictType = ConflictType.EDIT_DELETE;
      } else {
        // Local was not modified, server deleted it. No conflict, just apply deletion.
        return null;
      }
    } else if (localChangedSinceLastSync && serverChangedSinceLastSync) {
      // Both local and server versions have changed since the last common ancestor (last sync).
      // And the server change is more recent than what we locally know as server's last mod time.
      if (!localRecord.serverLastModifiedAt || serverModifiedAt > localRecord.serverLastModifiedAt) {
         conflictType = ConflictType.EDIT_EDIT;
      } else {
        // This case might mean local changes were based on an older server version,
        // but the incoming server data isn't actually newer than what we synced for this record.
        // This needs careful handling in SyncEngine - generally, if local changes exist, and server sends an update,
        // it's a potential conflict if both are "dirty" relative to last sync.
        // If serverModifiedAt <= localRecord.serverLastModifiedAt, it might be a stale server update.
        // For simplicity here, if both changed since last sync, we flag.
        conflictType = ConflictType.EDIT_EDIT;
      }
    }

    if (conflictType) {
      console.log(`Conflict detected for ${localRecord.constructor.table}/${localRecord.id}: ${conflictType}`);
      const conflictRecordCollection = this.database.collections.get<ConflictRecord>(TableName.CONFLICT_RECORDS);
      
      // Serialize relevant parts of the local record, avoid circular refs or large objects
      const localDataSnapshot = JSON.stringify({ 
        // Include fields relevant for comparison/resolution
        ...localRecord._raw, // _raw contains the raw DB values
        _modelName: localRecord.constructor.name 
      });
      const serverDataSnapshot = JSON.stringify(remoteData);

      // Check if a conflict record already exists for this entity
      let existingConflict = await conflictRecordCollection.query(
        ConflictRecord.find(localRecord.id) // Assuming entityId in ConflictRecord is the primary key for this query
      ).fetch();

      //This is wrong, ConflictRecord's ID is its own UUID, entityId is the ID of the conflicted record.
      const existingConflicts = await conflictRecordCollection
        .query(
            this.database.collections.get<ConflictRecord>(TableName.CONFLICT_RECORDS).query(
                Q.where('entity_id', localRecord.id),
                Q.where('entity_table_name', localRecord.constructor.table)
            )
        ).fetch();
      
      let conflictToSave: ConflictRecord;

      if (existingConflicts.length > 0) {
        conflictToSave = existingConflicts[0];
        await conflictToSave.update(cr => {
            cr.localData = localDataSnapshot;
            cr.serverData = serverDataSnapshot;
            cr.conflictType = conflictType!; // We know it's not null here
            cr.resolutionStatus = 'PENDING'; // Reset status if re-detected
            cr.updatedAt = Date.now();
        });

      } else {
         conflictToSave = await conflictRecordCollection.create(cr => {
            cr.entityTableName = localRecord.constructor.table; // Assumes model.table is the table name
            cr.entityId = localRecord.id;
            cr.localData = localDataSnapshot;
            cr.serverData = serverDataSnapshot;
            cr.conflictType = conflictType!; // We know it's not null here
            cr.resolutionStatus = 'PENDING';
            // WatermelonDB handles cr.id, cr.createdAt, cr.updatedAt
        });
      }
      return conflictToSave;
    }

    return null;
  }

  // UNIQUENESS_VIOLATION conflicts are typically created by UniquenessValidator or SyncEngine
  // based on server response, not purely by timestamp comparison.
  public async createUniquenessConflict(
    entityTableName: string,
    entityId: string,
    localData: any, // The local data that caused the violation
    serverConflictDetails: any // Details from server about the conflict
  ): Promise<ConflictRecord> {
    const conflictRecordCollection = this.database.collections.get<ConflictRecord>(TableName.CONFLICT_RECORDS);
    const localDataSnapshot = JSON.stringify(localData);
    const serverDataSnapshot = JSON.stringify(serverConflictDetails); // Or just a message

    // Check for existing similar conflict
    const existingConflicts = await conflictRecordCollection
    .query(
        this.database.collections.get<ConflictRecord>(TableName.CONFLICT_RECORDS).query(
            Q.where('entity_id', entityId),
            Q.where('entity_table_name', entityTableName),
            Q.where('conflict_type', ConflictType.UNIQUENESS_VIOLATION)
        )
    ).fetch();

    if(existingConflicts.length > 0) {
        const conflict = existingConflicts[0];
        await conflict.update(cr => {
            cr.localData = localDataSnapshot;
            cr.serverData = serverDataSnapshot; // Update with latest server info
            cr.resolutionStatus = 'PENDING';
            cr.updatedAt = Date.now();
        });
        return conflict;
    }


    return conflictRecordCollection.create(cr => {
      cr.entityTableName = entityTableName;
      cr.entityId = entityId;
      cr.localData = localDataSnapshot;
      cr.serverData = serverDataSnapshot;
      cr.conflictType = ConflictType.UNIQUENESS_VIOLATION;
      cr.resolutionStatus = 'PENDING';
    });
  }
}