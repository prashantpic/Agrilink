```typescript
import React, { useState, useEffect } from 'react';
import { View, Text, TextInput, Button, StyleSheet, ScrollView, Alert } from 'react-native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RouteProp } from '@react-navigation/native';
import { database } from '../../../../core/data/local/db';
import LandRecord from '../../../../core/data/local/models/LandRecord';
import SyncQueueItem from '../../../../core/data/local/models/SyncQueueItem';
import { OfflineIdGenerator } from '../../../../core/sync/OfflineIdGenerator';
import { SYNC_STATUS_PENDING_CREATE, SYNC_STATUS_PENDING_UPDATE, OPERATION_TYPE_CREATE, OPERATION_TYPE_UPDATE } from '../../../../core/constants/syncConstants';

type RootStackParamList = {
  LandFormScreen: { landRecordId?: string; farmerId: string }; // farmerId is required to link the land
  MapScreen: { landRecordId: string };
  // ... other screens
};

type LandFormScreenNavigationProp = StackNavigationProp<RootStackParamList, 'LandFormScreen'>;
type LandFormScreenRouteProp = RouteProp<RootStackParamList, 'LandFormScreen'>;

interface Props {
  navigation: LandFormScreenNavigationProp;
  route: LandFormScreenRouteProp;
}

const LandFormScreen: React.FC<Props> = ({ navigation, route }) => {
  const { landRecordId, farmerId: currentFarmerId } = route.params; // farmerId from navigation params
  const [isEditing, setIsEditing] = useState(!!landRecordId);

  const [parcelId, setParcelId] = useState('');
  const [totalArea, setTotalArea] = useState(''); // string input, convert to number
  const [cultivableArea, setCultivableArea] = useState(''); // string input

  // TODO: Add other LandRecord fields (ownershipTypeId, soilTypeId, irrigationSourceId, statusId)
  // These might be dropdowns fetching from master data (not covered here)

  useEffect(() => {
    if (!currentFarmerId) {
        Alert.alert("Error", "Farmer ID is missing. Cannot create or edit land record.");
        navigation.goBack();
        return;
    }
    if (isEditing && landRecordId) {
      const loadLandRecordData = async () => {
        try {
          const recordToEdit = await database.get<LandRecord>('land_records').find(landRecordId);
          setParcelId(recordToEdit.parcelId || '');
          setTotalArea(recordToEdit.totalArea?.toString() || '');
          setCultivableArea(recordToEdit.cultivableArea?.toString() || '');
          // TODO: Load other fields
        } catch (error) {
          console.error('Failed to load land record data for editing:', error);
          Alert.alert('Error', 'Could not load land record data.');
        }
      };
      loadLandRecordData();
    }
  }, [isEditing, landRecordId, currentFarmerId, navigation]);

  const handleNavigateToMap = (idToPass: string) => {
    navigation.navigate('MapScreen', { landRecordId: idToPass });
  };

  const handleSubmit = async () => {
    if (!currentFarmerId) {
        Alert.alert("Error", "Farmer ID is missing.");
        return;
    }
    if (!totalArea.trim()) {
      Alert.alert('Validation Error', 'Total area is required.');
      return;
    }

    const parsedTotalArea = parseFloat(totalArea);
    const parsedCultivableArea = cultivableArea.trim() ? parseFloat(cultivableArea) : undefined;

    if (isNaN(parsedTotalArea)) {
        Alert.alert('Validation Error', 'Invalid total area.');
        return;
    }
    if (cultivableArea.trim() && isNaN(parsedCultivableArea as number)) {
        Alert.alert('Validation Error', 'Invalid cultivable area.');
        return;
    }


    const landData = {
      farmerId: currentFarmerId,
      parcelId: parcelId.trim() || null, // Make it nullable if optional
      totalArea: parsedTotalArea,
      cultivableArea: parsedCultivableArea,
      // TODO: Add other fields
    };

    try {
      let finalLandRecordId = landRecordId;
      await database.write(async writer => {
        let landRecord: LandRecord;
        let operationType: string;

        if (isEditing && landRecordId) {
          const existingRecord = await writer.collections.get<LandRecord>('land_records').find(landRecordId);
          landRecord = await existingRecord.update(record => {
            record.parcelId = landData.parcelId;
            record.totalArea = landData.totalArea;
            record.cultivableArea = landData.cultivableArea;
            // TODO: Update other fields
            record.sync_status = SYNC_STATUS_PENDING_UPDATE;
            record.last_modified_locally_at = Date.now();
            record.updated_at = Date.now();
          });
          operationType = OPERATION_TYPE_UPDATE;
        } else {
          const localId = OfflineIdGenerator.generate();
          finalLandRecordId = localId; // Use this for map navigation if new
          landRecord = await writer.collections.get<LandRecord>('land_records').create(record => {
            record._raw.id = localId;
            // @ts-ignore // Farmer relation might need to be set differently if not direct ID
            record.farmer.id = landData.farmerId; // This assumes a `farmer` relation exists and can be set by ID
            record.parcelId = landData.parcelId;
            record.totalArea = landData.totalArea;
            record.cultivableArea = landData.cultivableArea;
            // TODO: Set other fields, including statusId
            record.sync_status = SYNC_STATUS_PENDING_CREATE;
            record.last_modified_locally_at = Date.now();
            record.created_at = Date.now();
            record.updated_at = Date.now();
          });
          operationType = OPERATION_TYPE_CREATE;
        }

        // REQ-14-001: Create SyncQueueItem
        await writer.collections.get<SyncQueueItem>('sync_queue_items').create(item => {
          item.entity_id = landRecord.id;
          item.entity_type = 'LandRecord';
          item.operation_type = operationType;
          item.payload = JSON.stringify(landData);
          item.attempt_count = 0;
          item.created_at = Date.now();
        });
      });

      Alert.alert(
        'Success',
        `Land record ${isEditing ? 'updated' : 'saved'} locally.`,
        [
          { text: 'OK', onPress: () => navigation.goBack() },
          { text: 'Add GPS Data', onPress: () => finalLandRecordId && handleNavigateToMap(finalLandRecordId) }
        ]
      );
      
    } catch (error) {
      console.error('Failed to save land record:', error);
      Alert.alert('Error', `Could not save land record. ${error}`);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.label}>Parcel ID (Optional):</Text>
      <TextInput style={styles.input} value={parcelId} onChangeText={setParcelId} placeholder="Enter parcel identifier" />

      <Text style={styles.label}>Total Area (e.g., acres/hectares):</Text>
      <TextInput style={styles.input} value={totalArea} onChangeText={setTotalArea} placeholder="Enter total area" keyboardType="numeric" />

      <Text style={styles.label}>Cultivable Area (Optional):</Text>
      <TextInput style={styles.input} value={cultivableArea} onChangeText={setCultivableArea} placeholder="Enter cultivable area" keyboardType="numeric" />

      {/* TODO: Add inputs for ownershipTypeId, soilTypeId, irrigationSourceId, statusId */}

      <Button title={isEditing ? 'Update Land Record' : 'Save Land Record'} onPress={handleSubmit} />
      {finalLandRecordId && (
        <View style={{marginTop: 10}}>
          <Button title="Add/Edit GPS Boundary" onPress={() => handleNavigateToMap(finalLandRecordId as string)} />
        </View>
      )}
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

export default LandFormScreen;
```