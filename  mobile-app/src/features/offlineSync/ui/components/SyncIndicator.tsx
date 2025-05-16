import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ActivityIndicator } from 'react-native';
import { syncStatusManager } from '../../../../core/sync/syncStatusManager';
import { OverallSyncStatus } from '../../../../core/constants/syncConstants';
import Icon from 'react-native-vector-icons/MaterialCommunityIcons'; // Example icon library

// REQ-14-008: Display of synchronization status and history. (This component shows current status)

interface SyncIndicatorProps {
  // Additional props can be added if needed, e.g., custom styles
}

const SyncIndicator: React.FC<SyncIndicatorProps> = () => {
  const [status, setStatus] = useState<OverallSyncStatus>(syncStatusManager.getCurrentStatus());

  useEffect(() => {
    const subscription = syncStatusManager.onStatusChange(newStatus => {
      setStatus(newStatus);
    });

    return () => {
      subscription.unsubscribe();
    };
  }, []);

  const getIndicatorContent = () => {
    switch (status) {
      case OverallSyncStatus.IDLE:
        return <Icon name="cloud-outline" size={24} color="gray" />;
      case OverallSyncStatus.SYNCING:
        return <ActivityIndicator size="small" color="blue" />;
      case OverallSyncStatus.SYNCED_SUCCESSFULLY:
        return <Icon name="cloud-check-outline" size={24} color="green" />;
      case OverallSyncStatus.OFFLINE:
        return <Icon name="cloud-off-outline" size={24} color="orange" />;
      case OverallSyncStatus.ERROR:
        return <Icon name="cloud-alert-outline" size={24} color="red" />;
      case OverallSyncStatus.CONFLICTS_DETECTED:
        return <Icon name="cloud-sync-outline" size={24} color="purple" />; // Icon indicates attention needed
      case OverallSyncStatus.UNIQUENESS_VALIDATION_NEEDED:
        return <Icon name="cloud-search-outline" size={24} color="teal" />;
      default:
        return <Icon name="cloud-question" size={24} color="gray" />;
    }
  };

  const getStatusText = () => {
    // Optional: show text alongside/instead of icon
    switch (status) {
        case OverallSyncStatus.IDLE: return "Idle";
        case OverallSyncStatus.SYNCING: return "Syncing...";
        case OverallSyncStatus.SYNCED_SUCCESSFULLY: return "Up to date";
        case OverallSyncStatus.OFFLINE: return "Offline";
        case OverallSyncStatus.ERROR: return "Sync Error";
        case OverallSyncStatus.CONFLICTS_DETECTED: return "Conflicts";
        case OverallSyncStatus.UNIQUENESS_VALIDATION_NEEDED: return "Validating...";
        default: return "Status Unknown";
    }
  }

  return (
    <View style={styles.container}>
      {getIndicatorContent()}
      {/* <Text style={styles.text}>{getStatusText()}</Text> */}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 10,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
  },
  text: {
    marginLeft: 5,
    fontSize: 12,
  }
});

export default SyncIndicator;