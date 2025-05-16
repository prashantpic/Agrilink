```typescript
import React, { useContext } from 'react';
import { View, Text, FlatList, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { StackNavigationProp } from '@react-navigation/stack';
import { Q } from '@nozbe/watermelondb';
import { useDatabase } from '@nozbe/watermelondb/hooks';
import { useOfflineData } from '../../../../core/hooks/useOfflineData'; // REQ-14-009 (Implicitly used if useOfflineData is implemented correctly)
import ConflictRecord from '../../../../core/data/local/models/ConflictRecord';
import { AuthContext, UserProfile } from '../../../../core/auth/AuthContext'; // REQ-14-010

// Define a placeholder for your RootStackParamList
type RootStackParamList = {
  ConflictListScreen: undefined;
  ConflictDetailScreen: { conflictId: string };
  // ... other screens
};

type ConflictListScreenNavigationProp = StackNavigationProp<RootStackParamList, 'ConflictListScreen'>;

interface Props {
  navigation: ConflictListScreenNavigationProp;
}

const ConflictListScreen: React.FC<Props> = ({ navigation }) => {
  const database = useDatabase();
  const { user } = useContext(AuthContext); // REQ-14-010

  // REQ-14-010: Role-based access control
  const canAccessConflictResolution = (currentUser: UserProfile | null): boolean => {
    if (!currentUser) return false;
    return ['Farm Plot Admin', 'Admin'].includes(currentUser.role); // Roles from SDS
  };

  // Query for unresolved conflicts. Assuming 'PENDING' is a status for unresolved conflicts.
  // This uses useOfflineData which wraps WatermelonDB's observation.
  const conflictsQuery = database.collections.get<ConflictRecord>('conflict_records')
    .query(Q.where('resolution_status', 'PENDING')); // Adjust 'PENDING' if your constant is different

  const pendingConflicts = useOfflineData<ConflictRecord[]>(conflictsQuery, []);

  if (!canAccessConflictResolution(user)) {
    // REQ-14-010: Deny access if role is not appropriate
    return (
      <View style={styles.container}>
        <Text style={styles.errorText}>You do not have permission to view this screen.</Text>
      </View>
    );
  }

  const renderConflictItem = ({ item }: { item: ConflictRecord }) => (
    <TouchableOpacity
      style={styles.itemContainer}
      onPress={() => navigation.navigate('ConflictDetailScreen', { conflictId: item.id })} // REQ-14-005: Navigate to detail
    >
      <Text style={styles.itemText}>Entity: {item.entityType}</Text>
      <Text style={styles.itemTextSmall}>Local ID: {item.entityId}</Text>
      <Text style={styles.itemTextSmall}>Conflict Type: {item.conflictType}</Text>
      <Text style={styles.itemTextSmall}>Detected: {new Date(item.createdAt).toLocaleString()}</Text>
    </TouchableOpacity>
  );

  if (!pendingConflicts || pendingConflicts.length === 0) {
    return (
      <View style={styles.container}>
        <Text>No pending conflicts to resolve.</Text>
      </View>
    );
  }

  return (
    <FlatList
      data={pendingConflicts}
      renderItem={renderConflictItem}
      keyExtractor={item => item.id}
      style={styles.list}
      ListHeaderComponent={<Text style={styles.header}>Pending Conflicts</Text>}
    />
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16,
  },
  list: {
    flex: 1,
  },
  header: {
    fontSize: 20,
    fontWeight: 'bold',
    textAlign: 'center',
    marginVertical: 16,
  },
  itemContainer: {
    backgroundColor: '#fff',
    padding: 16,
    marginVertical: 8,
    marginHorizontal: 16,
    borderRadius: 8,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.22,
    shadowRadius: 2.22,
  },
  itemText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#333',
  },
  itemTextSmall: {
    fontSize: 13,
    color: '#555',
    marginTop: 4,
  },
  errorText: {
    fontSize: 16,
    color: 'red',
    textAlign: 'center',
  },
});

export default ConflictListScreen;
```