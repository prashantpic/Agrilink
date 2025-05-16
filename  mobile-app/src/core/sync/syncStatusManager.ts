import { SyncStatus, SyncError } from '../../types/sync'; // Assuming types are defined in types/sync.ts
import { SYNC_STATUS_IDLE, SYNC_STATUS_SYNCING, SYNC_STATUS_SUCCESS, SYNC_STATUS_ERROR, SYNC_STATUS_CONFLICTS_PENDING, SYNC_STATUS_REQUIRES_UNIQUENESS_VALIDATION } from '../../constants/syncConstants';

// REQ-14-008: Manages and broadcasts data synchronization status.

interface SyncState {
  status: SyncStatus;
  lastSyncTimestamp: number | null;
  error: SyncError | null;
  pendingItemsCount: number;
  conflictsCount: number;
}

type Listener = (state: SyncState) => void;

class SyncStatusManager {
  private listeners: Set<Listener> = new Set();
  private currentState: SyncState = {
    status: SYNC_STATUS_IDLE,
    lastSyncTimestamp: null,
    error: null,
    pendingItemsCount: 0,
    conflictsCount: 0,
  };

  constructor() {
    // You could potentially load initial state from AsyncStorage here
    // e.g., lastSyncTimestamp
  }

  private notifyListeners(): void {
    this.listeners.forEach(listener => listener(this.currentState));
  }

  subscribe(listener: Listener): () => void {
    this.listeners.add(listener);
    listener(this.currentState); // Immediately notify with current state
    return () => {
      this.listeners.delete(listener);
    };
  }

  updateStatus(status: SyncStatus, error: SyncError | null = null): void {
    this.currentState.status = status;
    this.currentState.error = error;
    if (status === SYNC_STATUS_SUCCESS) {
      this.currentState.lastSyncTimestamp = Date.now();
      // Persist lastSyncTimestamp if needed
    }
    if (status === SYNC_STATUS_ERROR && error) {
        console.error("Sync Error:", error.message, error.details);
    }
    this.notifyListeners();
  }

  setPendingItemsCount(count: number): void {
    this.currentState.pendingItemsCount = count;
    this.notifyListeners();
  }

  setConflictsCount(count: number): void {
    this.currentState.conflictsCount = count;
    if (count > 0 && this.currentState.status !== SYNC_STATUS_ERROR) { // Don't override error state if conflicts are also present
        this.updateStatus(SYNC_STATUS_CONFLICTS_PENDING);
    }
    this.notifyListeners();
  }


  getCurrentState(): SyncState {
    return { ...this.currentState };
  }

  // Specific status updaters for convenience
  setIdle(): void {
    this.updateStatus(SYNC_STATUS_IDLE);
  }

  setSyncing(): void {
    this.updateStatus(SYNC_STATUS_SYNCING);
  }

  setSuccess(): void {
    this.updateStatus(SYNC_STATUS_SUCCESS);
    this.setConflictsCount(0); // Reset conflicts on successful sync
    this.setPendingItemsCount(0); // Reset pending items on successful sync
  }

  setError(message: string, details?: any): void {
    this.updateStatus(SYNC_STATUS_ERROR, { message, details });
  }
  
  setConflictsPending(): void {
    this.updateStatus(SYNC_STATUS_CONFLICTS_PENDING);
  }

  setRequiresUniquenessValidation(): void {
    // This status might be more record-specific than global,
    // but if there's at least one such record, the global status might reflect it.
    // Or, it's handled as a specific type of conflict.
    if (this.currentState.status !== SYNC_STATUS_ERROR) {
        this.updateStatus(SYNC_STATUS_REQUIRES_UNIQUENESS_VALIDATION);
    }
  }
}

const syncStatusManager = new SyncStatusManager();
export default syncStatusManager;