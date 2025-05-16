import { Model } from '@nozbe/watermelondb';
import { field, text, relation, readonly, date, writer } from '@nozbe/watermelondb/decorators';
import { Associations } from '@nozbe/watermelondb/Model';
import { TableName } from '../TableName';
import LandRecord from './LandRecord'; // eslint-disable-line import/no-cycle
// import { generateUUID } from '../../utils/uuidGenerator';

export enum GPSType {
  POINT = 'POINT',
  POLYGON = 'POLYGON',
  POLYLINE = 'POLYLINE',
}

export default class GPSData extends Model {
  static table = TableName.GPS_DATA;

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

  // GPSData Specific Fields
  @text('land_record_id') landRecordId!: string;
  @field('latitude') latitude!: number;
  @field('longitude') longitude!: number;
  @field('altitude') altitude?: number | null;
  @field('accuracy') accuracy?: number | null;
  @date('timestamp') gpsTimestamp!: number; // Timestamp from GPS device
  @text('type') type!: GPSType; // 'POINT', 'POLYGON', 'POLYLINE'
  @text('geo_data') geoData?: string | null; // GeoJSON string for POLYGON/POLYLINE, or additional point data

  @relation(TableName.LAND_RECORDS, 'land_record_id') landRecord!: LandRecord;

  @writer async createGPSDataPoint(data: {
    landRecordId: string;
    latitude: number;
    longitude: number;
    gpsTimestamp: number;
    type: GPSType;
    // id: string;
    syncStatus: string;
    altitude?: number;
    accuracy?: number;
    geoData?: string;
  }) {
    const now = Date.now();
    return this.collection.create<GPSData>(gps => {
      // gps.id = data.id;
      gps.landRecordId = data.landRecordId;
      gps.latitude = data.latitude;
      gps.longitude = data.longitude;
      gps.gpsTimestamp = data.gpsTimestamp;
      gps.type = data.type;
      gps.syncStatus = data.syncStatus;
      if (data.altitude !== undefined) gps.altitude = data.altitude;
      if (data.accuracy !== undefined) gps.accuracy = data.accuracy;
      if (data.geoData !== undefined) gps.geoData = data.geoData;
      gps.lastModifiedLocallyAt = now;
      gps.createdAt = now;
      gps.updatedAt = now;
    });
  }

  @writer async updateGPSData(data: Partial<Omit<GPSData, keyof Model | 'id' | 'createdAt' | 'landRecordId'>>) {
    const now = Date.now();
    return this.update(gps => {
      Object.assign(gps, data);
      gps.updatedAt = now;
      gps.lastModifiedLocallyAt = now;
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