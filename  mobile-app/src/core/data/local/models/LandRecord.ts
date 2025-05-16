import { Model, Q } from '@nozbe/watermelondb';
import { field, text, relation, children, readonly, date, writer } from '@nozbe/watermelondb/decorators';
import { Associations } from '@nozbe/watermelondb/Model';
import { TableName } from '../TableName';
import Farmer from './Farmer'; // eslint-disable-line import/no-cycle
import GPSData from './GPSData'; // eslint-disable-line import/no-cycle
import CropCycle from './CropCycle'; // eslint-disable-line import/no-cycle
// import { generateUUID } from '../../utils/uuidGenerator';

export default class LandRecord extends Model {
  static table = TableName.LAND_RECORDS;

  static associations: Associations = {
    [TableName.FARMERS]: { type: 'belongs_to', key: 'farmer_id' },
    [TableName.GPS_DATA]: { type: 'has_many', foreignKey: 'land_record_id' },
    [TableName.CROP_CYCLES]: { type: 'has_many', foreignKey: 'land_record_id' },
  };

  // Base Model Fields
  @readonly @text('id') id!: string;
  @text('remote_id') remoteId?: string | null;
  @text('sync_status') syncStatus!: string;
  @date('last_modified_locally_at') lastModifiedLocallyAt!: number;
  @date('server_last_modified_at') serverLastModifiedAt?: number | null;
  @readonly @date('created_at') createdAt!: number;
  @date('updated_at') updatedAt!: number;

  // LandRecord Specific Fields
  @text('farmer_id') farmerId!: string;
  @text('parcel_id') parcelId?: string | null; // Optional local identifier
  @field('total_area') totalArea!: number; // e.g., in hectares or acres, ensure consistency
  @field('cultivable_area') cultivableArea?: number | null;
  @field('ownership_type_id') ownershipTypeId?: number | null; // Lookup ID
  @field('soil_type_id') soilTypeId?: number | null; // Lookup ID
  @field('irrigation_source_id') irrigationSourceId?: number | null; // Lookup ID
  @field('status_id') statusId?: number | null; // Domain-specific status

  @relation(TableName.FARMERS, 'farmer_id') farmer!: Farmer;
  @children(TableName.GPS_DATA) gpsDataPoints!: Q.Query<GPSData>;
  @children(TableName.CROP_CYCLES) cropCycles!: Q.Query<CropCycle>;

  @writer async createLandRecord(data: {
    farmerId: string;
    totalArea: number;
    // id: string;
    syncStatus: string;
    // other fields...
  }) {
    const now = Date.now();
    return this.collection.create<LandRecord>(landRecord => {
      // landRecord.id = data.id;
      landRecord.farmerId = data.farmerId;
      landRecord.totalArea = data.totalArea;
      landRecord.syncStatus = data.syncStatus;
      landRecord.lastModifiedLocallyAt = now;
      landRecord.createdAt = now;
      landRecord.updatedAt = now;
      // map other fields
    });
  }

  @writer async updateLandRecord(data: Partial<Omit<LandRecord, keyof Model | 'id' | 'createdAt' | 'farmerId'>>) {
    const now = Date.now();
    return this.update(landRecord => {
      Object.assign(landRecord, data);
      landRecord.updatedAt = now;
      landRecord.lastModifiedLocallyAt = now;
    });
  }

  @writer async markAsDeleted() {
    return this.update(record => {
        record.syncStatus = 'PENDING_DELETE';
        record.updatedAt = Date.now();
        record.lastModifiedLocallyAt = Date.now();
    });
  }
}