```typescript
import React, { useState, useEffect, useContext, useCallback } from 'react';
import { View, Text, StyleSheet, ScrollView, ActivityIndicator, Alert } from 'react-native';
import { RouteProp, useNavigation, useRoute, NavigationProp } from '@react-navigation/native';
import { database } from '../../../../core/data/local/db';
import { ConflictRecord } from '../../../../core/data/local/models/ConflictRecord';
import { useOfflineData } from '../../../../hooks/useOfflineData';
import { ConflictResolutionForm, ResolutionData } from '../components/ConflictResolutionForm';
import { ConflictResolver }
from '../../../../core/sync/ConflictResolver';
import { AuthContext } from '../../../../core/auth/AuthContext';
import { AppStackParamList } from '../../../../navigation/AppNavigator'; // Assuming AppStackParamList is defined here

// Define navigation props for this screen
type ConflictDetailScreenRouteProp = RouteProp<AppStackParamList, 'ConflictDetail'>;
type ConflictDetailScreenNavigationProp = NavigationProp<AppStackParamList, 'ConflictDetail'>;

const ConflictDetailScreen: React.FC = () => {
  const route = useRoute<ConflictDetailScreenRouteProp>();
  const navigation = useNavigation<ConflictDetailScreenNavigationProp>();
  const { conflictRecordId } = route.params;

  const { user } = useContext(AuthContext); // For role-based checks if needed, though primarily navigator's job

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isResolving, setIsResolving] = useState(false);

  const conflictRecordQuery = useCallback(() => {
    if (!conflictRecordId) return null;
    return database.get<ConflictRecord>('conflict_records').findAndObserve(conflictRecordId);
  }, [conflictRecordId]);

  const conflictRecord = useOfflineData(conflictRecordQuery);

  useEffect(() => {
    if (conflictRecord !== undefined) { // undefined means still loading/not found by useOfflineData
      setIsLoading(false);
      if (conflictRecord === null && conflictRecordId) { // null means query ran but found nothing
        setError('Conflict record not found.');
      }
    }
  }, [conflictRecord, conflictRecordId]);

  const handleResolveConflict = async (resolutionData: ResolutionData) => {
    if (!conflictRecord) {
      Alert.alert("Error", "Conflict record is not available for resolution.");
      return;
    }

    // REQ-14-010: UI for manual conflict resolution. User role check should ideally gate access to this screen.
    // For example, if a specific role is required:
    // if (user?.role !== 'Admin' && user?.role !== 'Farm Plot Admin') { // Example roles
    //   Alert.alert("Access Denied", "You do not have permission to resolve conflicts.");
    //   return;
    // }

    setIsResolving(true);
    setError(null);

    try {
      // REQ-14-005: Conflict Resolution
      await ConflictResolver.applyManualResolution(
        database,
        conflictRecord,
        resolutionData.strategy,
        resolutionData.mergedData
      );
      Alert.alert("Success", "Conflict resolved successfully.");
      navigation.goBack();
    } catch (e: any) {
      console.error("Error resolving conflict:", e);
      setError(`Failed to resolve conflict: ${e.message || 'Unknown error'}`);
      Alert.alert("Error", `Failed to resolve conflict: ${e.message || 'Unknown error'}`);
    } finally {
      setIsResolving(false);
    }
  };

  if (isLoading) {
    return (
      <View style={styles.centered}>
        <ActivityIndicator size="large" />
        <Text>Loading conflict details...</Text>
      </View>
    );
  }

  if (error) {
    return (
      <View style={styles.centered}>
        <Text style={styles.errorText}>{error}</Text>
      </View>
    );
  }

  if (!conflictRecord) {
    return (
      <View style={styles.centered}>
        <Text>Conflict record not found or has been resolved.</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Text style={styles.title}>Conflict Details</Text>
      <View style={styles.infoContainer}>
        <Text style={styles.infoLabel}>Entity Type:</Text>
        <Text style={styles.infoValue}>{conflictRecord.entity_type}</Text>
      </View>
      <View style={styles.infoContainer}>
        <Text style={styles.infoLabel}>Entity ID (Local):</Text>
        <Text style={styles.infoValue}>{conflictRecord.entity_id}</Text>
      </View>
       <View style={styles.infoContainer}>
        <Text style={styles.infoLabel}>Conflict Type:</Text>
        <Text style={styles.infoValue}>{conflictRecord.conflict_type}</Text>
      </View>
      <View style={styles.infoContainer}>
        <Text style={styles.infoLabel}>Status:</Text>
        <Text style={styles.infoValue}>{conflictRecord.resolution_status}</Text>
      </View>
       <View style={styles.infoContainer}>
        <Text style={styles.infoLabel}>Detected At:</Text>
        <Text style={styles.infoValue}>{new Date(conflictRecord.created_at).toLocaleString()}</Text>
      </View>


      <ConflictResolutionForm
        conflictRecord={conflictRecord}
        onSubmit={handleResolveConflict}
        isResolving={isResolving}
      />
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  contentContainer: {
    padding: 20,
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
    color: '#333',
  },
  infoContainer: {
    flexDirection: 'row',
    marginBottom: 10,
    backgroundColor: '#fff',
    padding: 10,
    borderRadius: 5,
    elevation: 1,
  },
  infoLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#555',
    marginRight: 10,
  },
  infoValue: {
    fontSize: 16,
    color: '#333',
    flexShrink: 1, // Allow text to wrap
  },
  errorText: {
    fontSize: 16,
    color: 'red',
    textAlign: 'center',
  },
});

export default ConflictDetailScreen;
```