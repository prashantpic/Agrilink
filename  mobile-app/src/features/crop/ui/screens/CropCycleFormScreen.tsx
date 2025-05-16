```typescript
import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, Button, StyleSheet, ScrollView, Alert } from 'react-native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RouteProp } from '@react-navigation/native';
import { database } from '../../../../core/data/local/db';
import CropCycle from '../../../../core/data/local/models/CropCycle';
import LandRecord from '../../../../core/data/local/models/LandRecord'; // To verify landRecordId
import SyncQueueItem from '../../../../core/data/local/models/SyncQueueItem';
import { OfflineIdGenerator } from '../../../../core/sync/OfflineIdGenerator';
import { SYNC_STATUS_PENDING_CREATE, SYNC_STATUS_PENDING_UPDATE, OPERATION_TYPE_CREATE, OPERATION_TYPE_UPDATE } from '../../../../core/constants/syncConstants';

type RootStackParamList = {
  CropCycleFormScreen: { cropCycleId?: string; landRecordId: string };
  // ... other screens
};

type CropCycleFormScreenNavigationProp = StackNavigationProp<RootStackParamList, 'CropCycleFormScreen'>;
type CropCycleFormScreenRouteProp = RouteProp<RootStackParamList, 'CropCycleFormScreen'>;

interface Props {
  navigation: CropCycleFormScreenNavigationProp;
  route: CropCycleFormScreenRouteProp;
}

const CropCycleFormScreen: React.FC<Props> = ({ navigation, route }) => {
  const { cropCycleId, landRecordId: currentLandRecordId } = route.params;
  const [isEditing, setIsEditing] = useState(!!cropCycleId);

  const [cropTypeId, setCropTypeId] = useState(''); // This would typically be a picker/selector
  const [sowingDate, setSowingDate] = useState(''); // YYYY-MM-DD
  const [harvestDate, setHarvestDate] = useState(''); // YYYY-MM-DD, optional
  const [expectedYield, setExpectedYield] = useState(''); // string input

  // TODO: Add other CropCycle fields (statusId)
  // cropTypeId and statusId would ideally use a picker populated from master data

  useEffect(() => {
    if (!currentLandRecordId) {
        Alert.alert("Error", "Land Record ID is missing. Cannot create or edit crop cycle.");
        navigation.goBack();
        return;
    }

    const verifyLandRecord = async () => {
        try {
            await database.get<LandRecord>('land_records').find(currentLandRecordId);
        } catch (error) {
            Alert.alert("Error", "Invalid Land Record ID. The associated land record does not exist.");
            navigation.goBack();
        }
    };
    verifyLandRecord();
    
    if (isEditing && cropCycleId) {
      const loadCropCycleData = async () => {
        try {
          const recordToEdit = await database.get<CropCycle>('crop_cycles').find(cropCycleId);
          setCropTypeId(recordToEdit.cropTypeId?.toString() || ''); // Assuming cropTypeId is number
          setSowingDate(new Date(recordToEdit.sowingDate).toISOString().split('T')[0]);
          setHarvestDate(recordToEdit.harvestDate ? new Date(recordToEdit.harvestDate).toISOString().split('T')[0] : '');
          setExpectedYield(recordToEdit.expectedYield?.toString() || '');
          // TODO: Load other fields like statusId
        } catch (error) {
          console.error('Failed to load crop cycle data for editing:', error);
          Alert.alert('Error', 'Could not load crop cycle data.');
        }
      };
      loadCropCycleData();
    }
  }, [isEditing, cropCycleId, currentLandRecordId, navigation]);

  const handleSubmit = async () => {
    if (!currentLandRecordId) {
        Alert.alert("Error", "Land Record ID is missing.");
        return;
    }
    if (!cropTypeId.trim() || !sowingDate.trim()) {
      Alert.alert('Validation Error', 'Crop Type and Sowing Date are required.');
      return;
    }
    
    const parsedExpectedYield = expectedYield.trim() ? parseFloat(expectedYield) : undefined;
    if (expectedYield.trim() && isNaN(parsedExpectedYield as number)) {
        Alert.alert('Validation Error', 'Invalid expected yield.');
        return;
    }

    const cropCycleData = {
      landRecordId: currentLandRecordId,
      cropTypeId: parseInt(cropTypeId, 10), // Assuming numeric ID
      sowingDate: new Date(sowingDate).getTime(),
      harvestDate: harvestDate.trim() ? new Date(harvestDate).getTime() : null,
      expectedYield: parsedExpectedYield,
      // TODO: Add statusId
    };

    if (isNaN(cropCycleData.cropTypeId)) {
        Alert.alert('Validation Error', 'Invalid Crop Type ID.');
        return;
    }

    try {
      await database.write(async writer => {
        let cropCycleRecord: CropCycle;
        let operationType: string;

        if (isEditing && cropCycleId) {
          const existingRecord = await writer.collections.get<CropCycle>('crop_cycles').find(cropCycleId);
          cropCycleRecord = await existingRecord.update(record => {
            record.cropTypeId = cropCycleData.cropTypeId;
            record.sowingDate = cropCycleData.sowingDate;
            record.harvestDate = cropCycleData.harvestDate;
            record.expectedYield = cropCycleData.expectedYield;
            // TODO: Update statusId
            record.sync_status = SYNC_STATUS_PENDING_UPDATE;
            record.last_modified_locally_at = Date.now();
            record.updated_at = Date.now();
          });
          operationType = OPERATION_TYPE_UPDATE;
        } else {
          const localId = OfflineIdGenerator.generate();
          cropCycleRecord = await writer.collections.get<CropCycle>('crop_cycles').create(record => {
            record._raw.id = localId;
            // @ts-ignore // landRecord relation
            record.landRecord.id = cropCycleData.landRecordId;
            record.cropTypeId = cropCycleData.cropTypeId;
            record.sowingDate = cropCycleData.sowingDate;
            record.harvestDate = cropCycleData.harvestDate;
            record.expectedYield = cropCycleData.expectedYield;
            // TODO: Set statusId
            record.sync_status = SYNC_STATUS_PENDING_CREATE;
            record.last_modified_locally_at = Date.now();
            record.created_at = Date.now();
            record.updated_at = Date.now();
          });
          operationType = OPERATION_TYPE_CREATE;
        }

        // REQ-14-001: Create SyncQueueItem
        await writer.collections.get<SyncQueueItem>('sync_queue_items').create(item => {
          item.entity_id = cropCycleRecord.id;
          item.entity_type = 'CropCycle';
          item.operation_type = operationType;
          item.payload = JSON.stringify(cropCycleData);
          item.attempt_count = 0;
          item.created_at = Date.now();
        });
      });

      Alert.alert('Success', `Crop cycle ${isEditing ? 'updated' : 'saved'} locally.`);
      navigation.goBack();
    } catch (error) {
      console.error('Failed to save crop cycle:', error);
      Alert.alert('Error', `Could not save crop cycle. ${error}`);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.label}>Crop Type ID (Enter Numeric ID, e.g., from Master Data):</Text>
      <TextInput style={styles.input} value={cropTypeId} onChangeText={setCropTypeId} placeholder="Enter Crop Type ID" keyboardType="numeric" />

      <Text style={styles.label}>Sowing Date (YYYY-MM-DD):</Text>
      <TextInput style={styles.input} value={sowingDate} onChangeText={setSowingDate} placeholder="YYYY-MM-DD" />

      <Text style={styles.label}>Expected Harvest Date (YYYY-MM-DD, Optional):</Text>
      <TextInput style={styles.input} value={harvestDate} onChangeText={setHarvestDate} placeholder="YYYY-MM-DD" />

      <Text style={styles.label}>Expected Yield (Optional):</Text>
      <TextInput style={styles.input} value={expectedYield} onChangeText={setExpectedYield} placeholder="e.g., 1000 kg" keyboardType="numeric" />
      
      {/* TODO: Add input for statusId (likely a picker) */}

      <Button title={isEditing ? 'Update Crop Cycle' : 'Save Crop Cycle'} onPress={handleSubmit} />
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  label: {
    fontSize: 16,
    marginBottom: 4,
    color: '#333',
  },
  input: {
    borderWidth: 1,
    borderColor: '#ccc',
    padding: 10,
    marginBottom: 16,
    borderRadius: 4,
    backgroundColor: '#fff',
  },
});

export default CropCycleFormScreen;
```