import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosError } from 'axios';
import { API_BASE_URL, API_TOKEN_STORAGE_KEY } from '../../../config/environment'; // REQ-14-002, REQ-14-007 (Base URL)

// Assuming a secure storage utility. For simplicity, using localStorage as a placeholder.
// In a real React Native app, use `react-native-keychain` or `@react-native-async-storage/async-storage`
// combined with encryption for sensitive tokens if keychain is not used directly.
const secureStorage = {
  getItem: async (key: string): Promise<string | null> => {
    // return await AsyncStorage.getItem(key);
    // Or for Keychain:
    // const credentials = await Keychain.getGenericPassword({ service: key });
    // return credentials ? credentials.password : null;
    return localStorage.getItem(key); // Placeholder
  },
  setItem: async (key: string, value: string): Promise<void> => {
    // await AsyncStorage.setItem(key, value);
    // Or for Keychain:
    // await Keychain.setGenericPassword(key, value, { service: key });
    localStorage.setItem(key, value); // Placeholder
  },
  removeItem: async (key: string): Promise<void> => {
    // await AsyncStorage.removeItem(key);
    // Or for Keychain:
    // await Keychain.resetGenericPassword({ service: key });
    localStorage.removeItem(key); // Placeholder
  },
};


const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL, // REQ-14-002: Points to API Gateway
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000, // 15 seconds timeout
});

// REQ-14-007: Ensure HTTPS. Axios will use HTTPS if baseURL starts with https://
if (!API_BASE_URL.startsWith('https://') && process.env.NODE_ENV === 'production') {
  console.error('API_BASE_URL must use HTTPS in production.');
  // Optionally, throw an error or implement a more robust check
}

// Request interceptor for adding the auth token
apiClient.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    // REQ-14-007: Token fetched from secure storage
    const token = await secureStorage.getItem(API_TOKEN_STORAGE_KEY);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor for global error handling (optional)
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    // console.error('API Error:', error.response?.status, error.message);
    
    // Example: Handle 401 Unauthorized globally (e.g., redirect to login)
    // if (error.response?.status === 401) {
    //   // await secureStorage.removeItem(API_TOKEN_STORAGE_KEY);
    //   // Dispatch a logout action or navigate to login screen
    //   // This depends on how navigation and auth state are managed globally
    //   console.warn('Unauthorized access - 401. Token might be invalid or expired.');
    //   // eventBus.dispatch('auth:unauthorized'); // Example using an event bus
    // }

    // REQ-14-002 related: Server might return specific error codes for sync issues
    // These could be handled here or in the calling service (e.g., SyncEngine)
    
    return Promise.reject(error);
  }
);

export default apiClient;