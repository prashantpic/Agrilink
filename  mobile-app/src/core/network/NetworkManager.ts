import NetInfo, { NetInfoState, NetInfoSubscription } from '@react-native-community/netinfo';

export type NetworkStateListener = (state: NetInfoState) => void;

class NetworkManagerInstance {
  private currentState: NetInfoState | null = null;
  private listeners: Set<NetworkStateListener> = new Set();
  private unsubscribeNetInfo: NetInfoSubscription | null = null;

  constructor() {
    this.init();
  }

  private init(): void {
    // Subscribe to network state changes
    this.unsubscribeNetInfo = NetInfo.addEventListener(state => {
      this.handleNetworkStateChange(state);
    });

    // Get initial state
    NetInfo.fetch().then(state => {
      this.handleNetworkStateChange(state);
    });
  }

  private handleNetworkStateChange(state: NetInfoState): void {
    // REQ-14-002: Monitor device network connectivity
    // REQ-14-008: Provide this information for UI and SyncService
    this.currentState = state;
    this.notifyListeners();
  }

  private notifyListeners(): void {
    if (this.currentState) {
      this.listeners.forEach(listener => listener(this.currentState!));
    }
  }

  /**
   * Subscribes a listener function to network state changes.
   * @param listener The function to call when the network state changes.
   * @returns A function to unsubscribe the listener.
   */
  public subscribe(listener: NetworkStateListener): () => void {
    this.listeners.add(listener);
    // Immediately notify with current state if available
    if (this.currentState) {
      listener(this.currentState);
    }
    return () => this.unsubscribe(listener);
  }

  /**
   * Unsubscribes a listener function from network state changes.
   * @param listener The listener function to remove.
   */
  public unsubscribe(listener: NetworkStateListener): void {
    this.listeners.delete(listener);
  }

  /**
   * Gets the current network state.
   * @returns The current NetInfoState, or null if not yet determined.
   */
  public getCurrentState(): NetInfoState | null {
    return this.currentState;
  }

  /**
   * Checks if the device is currently connected to the internet.
   * @returns True if connected, false otherwise. Returns false if state is unknown.
   */
  public isOnline(): boolean {
    return this.currentState?.isConnected === true && this.currentState?.isInternetReachable === true;
  }

  /**
   * Cleans up the NetInfo subscription. Should be called if the manager is no longer needed globally.
   */
  public dispose(): void {
    if (this.unsubscribeNetInfo) {
      this.unsubscribeNetInfo();
      this.unsubscribeNetInfo = null;
    }
    this.listeners.clear();
    this.currentState = null;
  }
}

// Export a singleton instance of the NetworkManager
const NetworkManager = new NetworkManagerInstance();
export default NetworkManager;