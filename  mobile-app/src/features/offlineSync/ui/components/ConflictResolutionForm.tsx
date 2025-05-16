```typescript
import React, { useState, useEffect, useMemo } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ScrollView, TextInput, Alert } from 'react-native';
import { ConflictRecordData, ResolutionStrategy, ResolutionOutcome } from '../../../types/sync';

// Simple RadioButton component for selection
interface RadioButtonProps {
  label: string;
  selected: boolean;
  onPress: () => void;
}

const RadioButton: React.FC<RadioButtonProps> = ({ label, selected, onPress }) => (
  <TouchableOpacity style={styles.radioButtonContainer} onPress={onPress}>
    <View style={[styles.radioButtonOuter, selected && styles.radioButtonOuterSelected]}>
      {selected && <View style={styles.radioButtonInner} />}
    </View>
    <Text style={styles.radioLabel}>{label}</Text>
  </TouchableOpacity>
);

interface ConflictResolutionFormProps {
  conflictRecord: ConflictRecordData;
  onResolve: (resolution: ResolutionOutcome) => void;
  availableStrategies?: ResolutionStrategy[];
}

const ConflictResolutionForm: React.FC<ConflictResolutionFormProps> = ({
  conflictRecord,
  onResolve,
  availableStrategies = [
    ResolutionStrategy.KEEP_LOCAL,
    ResolutionStrategy.TAKE_SERVER,
    ResolutionStrategy.MERGE_MANUAL,
  ],
}) => {
  const [selectedStrategy, setSelectedStrategy] = useState<ResolutionStrategy | null>(null);
  const [mergedData, setMergedData] = useState<Record<string, any>>({});

  const { local_data, server_data, entity_type, entity_id } = conflictRecord;

  const allKeys = useMemo(() => {
    const keys = new Set<string>();
    if (local_data && typeof local_data === 'object') {
      Object.keys(local_data).forEach(key => keys.add(key));
    }
    if (server_data && typeof server_data === 'object') {
      Object.keys(server_data).forEach(key => keys.add(key));
    }
    // Exclude common internal/read-only fields from direct merge editing if desired,
    // but display them for context. For example: 'id', 'remote_id', 'created_at', 'updated_at', 'sync_status' etc.
    // For now, we include all keys in `mergedData` initialization for manual editing but this can be refined.
    return Array.from(keys);
  }, [local_data, server_data]);

  useEffect(() => {
    if (selectedStrategy === ResolutionStrategy.MERGE_MANUAL) {
      const initialMergeData: Record<string, any> = {};
      allKeys.forEach(key => {
        // Prioritize local, then server, then empty string for editable fields
        // This provides a starting point for the user to merge.
        if (local_data && local_data[key] !== undefined) {
          initialMergeData[key] = local_data[key];
        } else if (server_data && server_data[key] !== undefined) {
          initialMergeData[key] = server_data[key];
        } else {
          initialMergeData[key] = '';
        }
      });
      setMergedData(initialMergeData);
    } else {
      setMergedData({}); // Clear if not merging manually
    }
  }, [selectedStrategy, local_data, server_data, allKeys]);

  const handleMergeFieldChange = (key: string, value: string) => {
    setMergedData(prev => ({ ...prev, [key]: value }));
  };

  const handleSubmit = () => {
    if (!selectedStrategy) {
      Alert.alert('Selection Required', 'Please select a resolution strategy.');
      return;
    }

    let outcomeData: Record<string, any> | undefined = undefined;
    switch (selectedStrategy) {
      case ResolutionStrategy.KEEP_LOCAL:
        outcomeData = local_data;
        break;
      case ResolutionStrategy.TAKE_SERVER:
        outcomeData = server_data;
        break;
      case ResolutionStrategy.MERGE_MANUAL:
        if (Object.keys(mergedData).length === 0 && allKeys.length > 0) {
          Alert.alert('Merge Error', 'Please provide merged data or choose another strategy.');
          return;
        }
        outcomeData = mergedData;
        break;
      case ResolutionStrategy.ABORT_AND_RETRY: // This strategy might not involve data modification
        outcomeData = undefined; // Or could be local_data if retry implies current state
        break;
      default:
        Alert.alert('Error', 'Invalid resolution strategy selected.');
        return;
    }
    
    // For strategies not directly modifying data like ABORT_AND_RETRY, outcomeData might be undefined.
    // The onResolve handler in the parent should know how to interpret this.
    onResolve({ strategy: selectedStrategy, mergedData: outcomeData });
  };

  const renderDataComparison = () => (
    <View style={styles.comparisonContainer}>
      <View style={styles.column}>
        <Text style={styles.columnHeader}>Local Version</Text>
        {allKeys.map(key => (
          <View key={`local-${key}`} style={styles.fieldContainer}>
            <Text style={styles.fieldKey}>{key}:</Text>
            <Text style={styles.fieldValue}>
              {local_data && local_data[key] !== undefined ? JSON.stringify(local_data[key], null, 2) : 'N/A'}
            </Text>
          </View>
        ))}
      </View>
      <View style={styles.column}>
        <Text style={styles.columnHeader}>Server Version</Text>
        {allKeys.map(key => (
          <View key={`server-${key}`} style={styles.fieldContainer}>
            <Text style={styles.fieldKey}>{key}:</Text>
            <Text style={styles.fieldValue}>
              {server_data && server_data[key] !== undefined ? JSON.stringify(server_data[key], null, 2) : 'N/A'}
            </Text>
          </View>
        ))}
      </View>
    </View>
  );

  const renderMergeForm = () => (
    <View style={styles.mergeFormContainer}>
      <Text style={styles.sectionTitle}>Manually Merge Data:</Text>
      {allKeys.map(key => {
        // Decide if a field should be editable. Example: 'id' or 'remote_id' usually aren't.
        const isEditable = !['id', 'remote_id', 'created_at', 'server_last_modified_at'].includes(key);
        return (
          <View key={`merge-${key}`} style={styles.inputGroup}>
            <Text style={styles.inputLabel}>{key}:</Text>
            <TextInput
              style={[styles.textInput, !isEditable && styles.textInputDisabled]}
              value={String(mergedData[key] ?? '')}
              onChangeText={text => isEditable && handleMergeFieldChange(key, text)}
              editable={isEditable}
              multiline={typeof mergedData[key] === 'string' && mergedData[key].length > 50}
              numberOfLines={typeof mergedData[key] === 'string' && mergedData[key].length > 50 ? 3 : 1}
            />
          </View>
        );
        })}
    </View>
  );

  return (
    <ScrollView style={styles.container} keyboardShouldPersistTaps="handled">
      <Text style={styles.title}>Resolve Conflict</Text>
      <Text style={styles.subTitle}>Entity: {entity_type} (ID: {entity_id})</Text>
      
      {renderDataComparison()}

      <View style={styles.strategySelectionContainer}>
        <Text style={styles.sectionTitle}>Choose Resolution Strategy:</Text>
        {availableStrategies.map(strategy => (
          <RadioButton
            key={strategy}
            label={strategy.replace(/_/g, ' ')} // Make label more readable
            selected={selectedStrategy === strategy}
            onPress={() => setSelectedStrategy(strategy)}
          />
        ))}
      </View>

      {selectedStrategy === ResolutionStrategy.MERGE_MANUAL && renderMergeForm()}

      <TouchableOpacity style={styles.submitButton} onPress={handleSubmit}>
        <Text style={styles.submitButtonText}>Apply Resolution</Text>
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#f8f9fa',
  },
  title: {
    fontSize: 22,
    fontWeight: 'bold',
    marginBottom: 8,
    textAlign: 'center',
    color: '#343a40',
  },
  subTitle: {
    fontSize: 14,
    marginBottom: 16,
    textAlign: 'center',
    color: '#6c757d',
  },
  comparisonContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 20,
    backgroundColor: '#fff',
    borderRadius: 8,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  column: {
    flex: 1,
    padding: 12,
  },
  columnHeader: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 10,
    color: '#007bff',
    borderBottomWidth: 1,
    borderBottomColor: '#dee2e6',
    paddingBottom: 6,
  },
  fieldContainer: {
    marginBottom: 8,
  },
  fieldKey: {
    fontWeight: 'bold',
    fontSize: 13,
    color: '#495057',
  },
  fieldValue: {
    fontSize: 13,
    color: '#212529',
    marginTop: 2,
    backgroundColor: '#e9ecef',
    padding: 4,
    borderRadius: 3,
    fontFamily: 'monospace', // Better for JSON string
  },
  strategySelectionContainer: {
    marginBottom: 20,
    padding: 15,
    backgroundColor: '#fff',
    borderRadius: 8,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  sectionTitle: {
    fontSize: 17,
    fontWeight: '600',
    marginBottom: 12,
    color: '#343a40',
  },
  radioButtonContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
    paddingVertical: 6,
  },
  radioButtonOuter: {
    height: 22,
    width: 22,
    borderRadius: 11,
    borderWidth: 2,
    borderColor: '#007bff',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  radioButtonOuterSelected: {
    // borderColor: '#0056b3',
  },
  radioButtonInner: {
    height: 12,
    width: 12,
    borderRadius: 6,
    backgroundColor: '#007bff',
  },
  radioLabel: {
    fontSize: 16,
    color: '#212529',
    textTransform: 'capitalize',
  },
  mergeFormContainer: {
    marginBottom: 20,
    padding: 15,
    backgroundColor: '#fff',
    borderRadius: 8,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
  },
  inputGroup: {
    marginBottom: 12,
  },
  inputLabel: {
    fontSize: 14,
    fontWeight: '500',
    marginBottom: 5,
    color: '#495057',
  },
  textInput: {
    borderWidth: 1,
    borderColor: '#ced4da',
    borderRadius: 4,
    paddingHorizontal: 10,
    paddingVertical: 8,
    fontSize: 14,
    backgroundColor: '#fff',
    color: '#495057',
  },
  textInputDisabled: {
    backgroundColor: '#e9ecef',
    color: '#6c757d',
  },
  submitButton: {
    backgroundColor: '#28a745',
    paddingVertical: 14,
    paddingHorizontal: 20,
    borderRadius: 5,
    alignItems: 'center',
    marginTop: 10,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 2,
  },
  submitButtonText: {
    color: '#fff',
    fontSize: 17,
    fontWeight: 'bold',
  },
});

export default ConflictResolutionForm;

```