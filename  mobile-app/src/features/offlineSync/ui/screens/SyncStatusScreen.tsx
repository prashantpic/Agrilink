import React, { useState, useCallback, useEffect } from 'react';
import { View, Text, Button, StyleSheet, ScrollView, ActivityIndicator, Alert } from 'react-native';
// NOTE: The following imports are placeholders for actual modules to be created in other files/iterations.
// This screen assumes that a 'useSyncState' hook exists, providing the synchronization state,
// and that 'SyncService' provides a method to initiate synchronization.
// 'SyncStatus' enum and 'ISyncState' type are expected to be defined in the types.
import { useSyncState } from '../../../../core/sync/hooks/useSyncState'; // Placeholder: e.g., src/core/sync/hooks/useSyncState.ts
import { SyncService } from '../../../../core/sync/SyncService'; // Placeholder: e.g., src/core/sync/SyncService.ts
import { SyncStatus, ISyncState } from '../../../../types'; // Placeholder: e.g., src/types/index.ts (re-exporting from src/types/sync.ts)

// REQ-14-008: Displays sync status from syncStatusManager, sync history, errors. Allows manual sync trigger.
const SyncStatusScreen: React.FC = () => {
  // The useSyncState hook is expected to provide the current synchronization state
  // from syncStatusManager.
  const syncState: ISyncState | undefined = useSyncState();
  const [isManuallySyncing, setIsManuallySyncing] = useState(false);

  const handleManualSync = useCallback(async () => {
    if (!SyncService || typeof SyncService.initiateSync !== 'function') {
        Alert.alert('Error', 'SyncService is not available. Cannot initiate sync.');
        return;
    }
    setIsManuallySyncing(true);
    try {
      await SyncService.initiateSync();
      Alert.alert('Sync Initiated', 'Synchronization process has started.');
      // The useSyncState hook should automatically reflect changes
      // pushed by syncStatusManager after the sync process starts and completes.
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'An unknown error occurred during sync initiation.';
      Alert.alert('Sync Error', errorMessage);
      // Errors during the sync process itself should be reflected in syncState.error
    } finally {
      setIsManuallySyncing(false);
    }
  }, []);

  const renderSyncStatus = () => {
    if (!syncState) {
      return (
        <View style={styles.statusContainer}>
          <ActivityIndicator size="large" color="#0000ff" />
          <Text style={styles.statusText}>Loading sync status...</Text>
        </View>
      );
    }

    if (isManuallySyncing || syncState.status === SyncStatus.SYNCING) {
      return (
        <View style={styles.statusContainer}>
          <ActivityIndicator size="large" color="#0000ff" />
          <Text style={styles.statusText}>Syncing data...</Text>
        </View>
      );
    }

    switch (syncState.status) {
      case SyncStatus.IDLE:
        return <Text style={styles.statusText}>Status: Idle. Ready to sync.</Text>;
      case SyncStatus.SYNCED:
        return <Text style={styles.statusText}>Status: Synced. All data is up-to-date.</Text>;
      case SyncStatus.OFFLINE:
        return <Text style={styles.statusText}>Status: Offline. Sync will resume when online.</Text>;
      case SyncStatus.ERROR:
        return <Text style={[styles.statusText, styles.errorText]}>Status: Error during last sync attempt.</Text>;
      case SyncStatus.CONFLICTS_PENDING:
        return <Text style={[styles.statusText, styles.warningText]}>Status: Conflicts pending resolution.</Text>;
      case SyncStatus.REQUIRES_UNIQUENESS_VALIDATION:
        return <Text style={[styles.statusText, styles.warningText]}>Status: Some items require server uniqueness validation.</Text>;
      default:
        const currentStatusText = syncState.status ? String(syncState.status) : 'Unknown';
        return <Text style={styles.statusText}>Status: {currentStatusText}</Text>;
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      <Text style={styles.title}>Synchronization Status</Text>

      <View style={styles.section}>
        {renderSyncStatus()}
      </View>

      {syncState && (
        <>
          <View style={styles.section}>
            <Text style={styles.label}>Last Successful Sync:</Text>
            <Text style={styles.value}>
              {syncState.lastSyncTimestamp
                ? new Date(syncState.lastSyncTimestamp).toLocaleString()
                : 'Never'}
            </Text>
          </View>

          <View style={styles.section}>
            <Text style={styles.label}>Pending Items for Sync:</Text>
            <Text style={styles.value}>{syncState.pendingItemsCount ?? 'N/A'}</Text>
          </View>

          {syncState.error && (
            <View style={styles.section}>
              <Text style={styles.labelError}>Last Sync Error:</Text>
              <Text style={[styles.value, styles.errorText]}>{syncState.error}</Text>
            </View>
          )}
        </>
      )}
      
      <View style={styles.section}>
        <Text style={styles.label}>Sync History/Log:</Text>
        {/* 
          REQ-14-008: Display of synchronization history.
          This section is a placeholder for displaying more detailed sync history or logs.
          The actual implementation will depend on how `syncStatusManager` or other services
          expose historical sync data (e.g., list of past sync operations, significant events).
        */}
        <Text style={styles.value}>
          (Detailed sync history or logs will be displayed here in a future update.)
        </Text>
      </View>

      <Button
        title={(isManuallySyncing || syncState?.status === SyncStatus.SYNCING) ? 'Syncing...' : 'Sync Manually Now'}
        onPress={handleManualSync}
        disabled={isManuallySyncing || !syncState || syncState.status === SyncStatus.SYNCING}
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
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
    color: '#333',
  },
  section: {
    marginBottom: 20,
    padding: 15,
    backgroundColor: '#ffffff',
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#444',
    marginBottom: 5,
  },
  labelError: {
    fontSize: 16,
    fontWeight: '600',
    color: '#c0392b', // Darker Red
    marginBottom: 5,
  },
  value: {
    fontSize: 16,
    color: '#555',
  },
  statusContainer: {
    alignItems: 'center',
    paddingVertical: 10,
  },
  statusText: {
    fontSize: 18,
    fontWeight: '500',
    marginTop: 5,
    textAlign: 'center',
    color: '#333',
  },
  errorText: {
    color: '#c0392b', // Darker Red
  },
  warningText: {
    color: '#f39c12', // Orange
  },
});

export default SyncStatusScreen;