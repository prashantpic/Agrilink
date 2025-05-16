import { Model } from '@nozbe/watermelondb';
import { field, text, relation, readonly, date, writer } from '@nozbe/watermelondb/decorators';
import { Associations } from '@nozbe/watermelondb/Model';
import { TableName } from '../TableName';
import LandRecord from './LandRecord'; // eslint-disable-line import/no-cycle
// import { generateUUID } from '../../utils/uuidGenerator';

export default class CropCycle extends Model {
  static table = TableName.CROP_CYCLES;

  static associations: Associations = {
    [TableName.LAND_RECORDS]: { type: 'belongs_to', key: 'land_record_id' },
  };

  // Base Model Fields
  @readonly @text('id') id!: string;
  @text('remote_id') remoteId?: string | null;
  @text('sync_status') syncStatus!: string;
  @date('last_modified_locally_at') lastModifiedLocallyAt!: number;
  @date('server_last_modified_at') serverLastModifiedAt?: number | null;
  @readonly @date('created_at') createdAt!: number;
  @date('updated_at') updatedAt!: number;

  // CropCycle Specific Fields
  @text('land_record_id') landRecordId!: string;
  @field('crop_type_id') cropTypeId!: number; // Lookup ID for CropType master data
  @text('crop_variety_name') cropVarietyName?: string | null;
  @date('sowing_date') sowingDate!: number; // Store as timestamp
  @date('expected_harvest_date') expectedHarvestDate?: number | null; // Store as timestamp
  @date('actual_harvest_date') actualHarvestDate?: number | null; // Store as timestamp
  @field('expected_yield_quantity') expectedYieldQuantity?: number | null;
  @text('expected_yield_unit') expectedYieldUnit?: string | null; // e.g., 'KG', 'TONNES'
  @field('actual_yield_quantity') actualYieldQuantity?: number | null;
  @text('actual_yield_unit') actualYieldUnit?: string | null;
  @field('status_id') statusId?: number | null; // Domain-specific status, e.g., 'PLANTED', 'HARVESTED'
  @text('notes') notes?: string | null;

  @relation(TableName.LAND_RECORDS, 'land_record_id') landRecord!: LandRecord;

  @writer async createCropCycle(data: {
    landRecordId: string;
    cropTypeId: number;
    sowingDate: number;
    // id: string;
    syncStatus: string;
    // other fields...
  }) {
    const now = Date.now();
    return this.collection.create<CropCycle>(cropCycle => {
      // cropCycle.id = data.id;
      cropCycle.landRecordId = data.landRecordId;
      cropCycle.cropTypeId = data.cropTypeId;
      cropCycle.sowingDate = data.sowingDate;
      cropCycle.syncStatus = data.syncStatus;
      cropCycle.lastModifiedLocallyAt = now;
      cropCycle.createdAt = now;
      cropCycle.updatedAt = now;
      // map other fields
    });
  }

  @writer async updateCropCycle(data: Partial<Omit<CropCycle, keyof Model | 'id' | 'createdAt' | 'landRecordId'>>) {
    const now = Date.now();
    return this.update(cropCycle => {
      Object.assign(cropCycle, data);
      cropCycle.updatedAt = now;
      cropCycle.lastModifiedLocallyAt = now;
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