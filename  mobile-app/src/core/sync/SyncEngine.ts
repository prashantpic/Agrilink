import { Q } from '@nozbe/watermelondb';
import database from '../data/local/db';
import apiClient from '../data/remote/apiClient';
import { ConflictDetector } from './ConflictDetector';
import { ConflictResolver } from './ConflictResolver';
import { UniquenessValidator } from './UniquenessValidator';
import { syncStatusManager } from './syncStatusManager';
import { OverallSyncStatus, SyncStatus } from '../constants/syncConstants';
import { SyncQueueItem } from '../data/local/models/SyncQueueItem'; // Assuming this model exists
import { BaseModel } from '../data/local/models/BaseModel'; // For type casting
import { NetworkManager } from '../network/NetworkManager'; // REQ-14-002
import AsyncStorage from '@react-native-async-storage/async-storage';
import { ConflictRecord } from '../data/local/models/ConflictRecord'; // REQ-14-004

const LAST_SYNC_TIMESTAMP_KEY = 'lastSyncTimestamp';

// Placeholder for DTO types - in a real app, these would be defined in types/api.ts or similar
type ServerRecordDto = any;
type LocalRecordDto = any;

// Configuration for entities to sync
// In a real app, this might be more sophisticated, including DTO mappers, etc.
const SYNCABLE_ENTITIES: { name: string; uniquenessCheck?: { field: string }[] }[] = [
  { name: 'farmers', uniquenessCheck: [{ field: 'primaryPhone' }] }, // Example from SDS REQ-14-005
  { name: 'land_records' },
  { name: 'gps_data' },
  { name: 'crop_cycles' },
];

export class SyncEngine {
  private conflictDetector: ConflictDetector;
  private conflictResolver: ConflictResolver;
  private uniquenessValidator: UniquenessValidator;
  private networkManager: NetworkManager;

  constructor() {
    this.conflictDetector = new ConflictDetector(database);
    this.conflictResolver = new ConflictResolver(database); // REQ-14-005, REQ-14-006
    this.uniquenessValidator = new UniquenessValidator(apiClient, database); // REQ-14-005
    this.networkManager = NetworkManager; // Singleton instance
  }

  private async getLastSyncTimestamp(): Promise<number> {
    const timestampStr = await AsyncStorage.getItem(LAST_SYNC_TIMESTAMP_KEY);
    return timestampStr ? parseInt(timestampStr, 10) : 0;
  }

  private async setLastSyncTimestamp(timestamp: number): Promise<void> {
    await AsyncStorage.setItem(LAST_SYNC_TIMESTAMP_KEY, timestamp.toString());
  }

  // REQ-14-002: Bi-directional data synchronization
  public async synchronize(): Promise<void> {
    if (!this.networkManager.isOnline) {
      syncStatusManager.updateStatus(OverallSyncStatus.OFFLINE);
      console.log('SyncEngine: Offline, skipping sync.');
      return;
    }

    syncStatusManager.updateStatus(OverallSyncStatus.SYNCING);
    console.log('SyncEngine: Starting synchronization.');

    try {
      await this.pushLocalChanges();
      await this.pullRemoteChanges();

      // Post-sync check for unresolved conflicts
      const unresolvedConflicts = await database.get<ConflictRecord>('conflict_records')
        .query(Q.where('resolution_status', Q.eq('PENDING')))
        .fetchCount();

      if (unresolvedConflicts > 0) {
        syncStatusManager.updateStatus(OverallSyncStatus.CONFLICTS_DETECTED);
        console.log(`SyncEngine: Synchronization finished with ${unresolvedConflicts} unresolved conflicts.`);
      } else {
        syncStatusManager.updateStatus(OverallSyncStatus.SYNCED_SUCCESSFULLY);
        console.log('SyncEngine: Synchronization completed successfully.');
      }

    } catch (error) {
      console.error('SyncEngine: Synchronization failed', error);
      syncStatusManager.updateStatus(OverallSyncStatus.ERROR);
    }
  }

  private async pushLocalChanges(): Promise<void> {
    console.log('SyncEngine: Pushing local changes.');
    const syncQueueCollection = database.get<SyncQueueItem>('sync_queue_items');
    const pendingItems = await syncQueueCollection.query().fetch();

    if (pendingItems.length === 0) {
      console.log('SyncEngine: No local changes to push.');
      return;
    }

    // Group by entity type for batching (simplified batching here)
    const changesToPush: { entityType: string; payload: LocalRecordDto[] }[] = [];

    for (const item of pendingItems) {
      try {
        const entityCollection = database.get<BaseModel>(item.entityType);
        const record = await entityCollection.find(item.entityId);

        // REQ-14-005: Uniqueness Validation before push for relevant entities/fields
        const entityConfig = SYNCABLE_ENTITIES.find(e => e.name === item.entityType);
        if (entityConfig?.uniquenessCheck && (item.operationType === 'CREATE' || item.operationType === 'UPDATE')) {
           let unique = true;
           for (const check of entityConfig.uniquenessCheck) {
            // @ts-ignore
            const valueToValidate = record[check.field];
            if (valueToValidate) {
              const isFieldUnique = await this.uniquenessValidator.checkUniqueness(
                record,
                item.entityType,
                check.field,
                valueToValidate
              );
              if (!isFieldUnique) {
                unique = false;
                // Record status updated by UniquenessValidator, it won't be pushed now.
                // The SyncQueueItem should be handled (e.g. removed or marked)
                // For now, we skip pushing this item if uniqueness fails
                console.log(`SyncEngine: Uniqueness validation failed for ${item.entityType} ${item.entityId}, field ${check.field}. Skipping push for this item.`);
                // Optionally remove from sync_queue or mark as needing resolution.
                // For now, we just let it be picked up again if resolved or it will fail again.
                // A better approach might be to mark the SyncQueueItem itself.
                // For now, deleting it from queue to avoid repeated failed attempts if the record status is REQUIRES_UNIQUENESS_VALIDATION
                await database.write(async () => await item.destroyPermanently());
                break; 
              }
            }
           }
           if (!unique) continue; // Skip to next item if any uniqueness check failed
        }


        // Transform record to DTO (simplified)
        const recordDto = { ...record._raw, operation: item.operationType, localId: record.id };
        delete recordDto._status; // WatermelonDB internal fields
        delete recordDto._changed;

        let entityPushPayload = changesToPush.find(p => p.entityType === item.entityType);
        if (!entityPushPayload) {
          entityPushPayload = { entityType: item.entityType, payload: [] };
          changesToPush.push(entityPushPayload);
        }
        entityPushPayload.payload.push(recordDto);

      } catch (error) {
        console.error(`SyncEngine: Error preparing record ${item.entityId} of type ${item.entityType} for push.`, error);
        // Optionally mark SyncQueueItem with error, increment attempt count
        await database.write(async () => {
            await item.update(i => {
                i.attemptCount += 1;
            });
        });
      }
    }

    if (changesToPush.length === 0) return;

    try {
      // Example: endpoint expects { changes: [{ entityType: 'farmers', payload: [farmer1, farmer2] }, ...] }
      const response = await apiClient.post('/sync/push', { changes: changesToPush });
      const results = response.data.results; // Assuming server returns results for each pushed item

      await database.write(async () => {
        for (const result of results) { // Process server response
          const correspondingQueueItem = pendingItems.find(p => p.entityId === result.localId && p.entityType === result.entityType);
          if (!correspondingQueueItem) continue;

          const entityCollection = database.get<BaseModel>(result.entityType);
          try {
            const record = await entityCollection.find(result.localId);
            if (result.success) {
              await record.update(r => {
                r.remoteId = result.remoteId;
                r.serverLastModifiedAt = result.serverLastModifiedAt || Date.now();
                r.syncStatus = SyncStatus.SYNCED;
              });
              await correspondingQueueItem.destroyPermanently();
            } else if (result.conflict) { // REQ-14-004 Conflict detected by server
              await this.conflictDetector.createConflictFromServer(record, result.conflictData || {}, 'SERVER_REJECTED');
              await record.setConflict();
              // Keep queue item or handle as per strategy
            } else { // Other errors
                await correspondingQueueItem.update(i => { i.attemptCount += 1; });
                // record syncStatus might need an update to SYNC_FAILED
            }
          } catch (e) {
            // Record might have been deleted locally while push was in progress
             if (correspondingQueueItem) await correspondingQueueItem.destroyPermanently(); // Clean up queue
            console.warn(`SyncEngine: Record ${result.localId} not found locally after push result, or error processing result.`, e);
          }
        }
      });
    } catch (error) {
      console.error('SyncEngine: Error pushing local changes to server.', error);
      // Handle batch push error - items remain in queue for next attempt
      syncStatusManager.updateStatus(OverallSyncStatus.ERROR); // Or a more specific push error
      throw error; // Re-throw to be caught by the main synchronize method
    }
  }

  private async pullRemoteChanges(): Promise<void> {
    console.log('SyncEngine: Pulling remote changes.');
    const lastSync = await this.getLastSyncTimestamp();
    
    // Fetch changes for all syncable entities
    // Server might provide a single endpoint or one per entity
    // Example: /sync/pull?lastSyncTimestamp=<ts>&entities=farmers,land_records...
    const response = await apiClient.get(`/sync/pull?lastSyncTimestamp=${lastSync}&entities=${SYNCABLE_ENTITIES.map(e=>e.name).join(',')}`);
    const serverChangesByEntity = response.data.changes; // E.g. { farmers: [...], land_records: [...] }
    const newServerSyncTimestamp = response.data.timestamp || Date.now();

    await database.write(async (batch) => {
      for (const entityName of Object.keys(serverChangesByEntity)) {
        const collection = database.get<BaseModel>(entityName);
        const remoteRecords: ServerRecordDto[] = serverChangesByEntity[entityName];

        for (const remoteRecord of remoteRecords) {
          let localRecord: BaseModel | null = null;
          if (remoteRecord.remote_id) { // Standard case: update or conflict check
            const existing = await collection.query(Q.where('remote_id', remoteRecord.remote_id)).fetch();
            localRecord = existing.length > 0 ? existing[0] : null;
          } else if (remoteRecord.id_that_was_local_uuid_on_creation_if_any) { 
            // Edge case: server confirming a creation that didn't get its remote_id updated locally yet
            const existing = await collection.query(Q.where('id', remoteRecord.id_that_was_local_uuid_on_creation_if_any)).fetch();
            localRecord = existing.length > 0 ? existing[0] : null;
          }


          if (localRecord) { // Record exists locally
            // REQ-14-004: Conflict Detection
            // Basic check: if local has unsynced changes and server also changed it
            const isLocallyModified = localRecord.syncStatus !== SyncStatus.SYNCED &&
                                      localRecord.lastModifiedLocallyAt > (localRecord.serverLastModifiedAt || 0);
            
            // Server's view of lastModified should be more recent than local serverLastModifiedAt to be a server-side change
            const isServerModified = remoteRecord.server_last_modified_at > (localRecord.serverLastModifiedAt || 0);

            if (isLocallyModified && isServerModified && localRecord.lastModifiedLocallyAt > lastSync && remoteRecord.server_last_modified_at > lastSync) {
              // More robust conflict detection using last_modified_locally_at vs remote server_last_modified_at
              // and considering the last successful sync timestamp as common ancestor
              const conflict = await this.conflictDetector.detectConflict(localRecord, remoteRecord, lastSync);
              if (conflict) {
                await localRecord.setConflict();
                // REQ-14-006: Last-write-wins for designated fields (could be part of ConflictResolver)
                // For now, manual resolution is default for detected conflicts
                continue; // Skip applying remote change directly
              }
            }
            // No conflict or resolved, apply update from server
            await localRecord.update(record => {
              Object.assign(record, this.transformServerToLocal(remoteRecord)); // DTO mapping
              record.syncStatus = SyncStatus.SYNCED;
              record.serverLastModifiedAt = remoteRecord.server_last_modified_at || newServerSyncTimestamp;
            });

          } else { // New record from server
            await collection.create(record => {
              Object.assign(record, this.transformServerToLocal(remoteRecord));
              // @ts-ignore
              record.id = remoteRecord.local_id_if_it_was_offline_creation || remoteRecord.id; // Server might send back local UUID for matching
              // @ts-ignore
              record.remoteId = remoteRecord.remote_id || remoteRecord.id; // Ensure remoteId is set
              record.syncStatus = SyncStatus.SYNCED;
              // @ts-ignore
              record.createdAt = remoteRecord.created_at || newServerSyncTimestamp;
               // @ts-ignore
              record.updatedAt = remoteRecord.updated_at || newServerSyncTimestamp;
              // @ts-ignore
              record.serverLastModifiedAt = remoteRecord.server_last_modified_at || newServerSyncTimestamp;
            });
          }
        }
      }
    });
    await this.setLastSyncTimestamp(newServerSyncTimestamp);
    console.log('SyncEngine: Remote changes pulled and processed.');
  }

  // Placeholder for DTO transformation
  private transformServerToLocal(serverData: ServerRecordDto): Partial<BaseModel> {
    const { id, ...rest } = serverData; // Server might use 'id' as its primary key
    const localData: any = { ...rest };
    if (id) localData.remoteId = id; // Map server's primary key to remote_id
    // Add other transformations (date parsing, field name mapping)
    return localData;
  }
}