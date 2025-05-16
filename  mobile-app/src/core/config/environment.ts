interface EnvironmentConfig {
  API_BASE_URL: string;
  API_TOKEN_STORAGE_KEY: string;
  LOCAL_DB_ENCRYPTION_KEY_ALIAS: string; // For native keychain/keystore alias
  // Add other environment-specific configurations here
  // e.g., feature flags, third-party API keys (if client-side)
  FEATURE_FLAG_NEW_MAP_UI: boolean;
}

// Example: Development environment configuration
const devEnvironment: EnvironmentConfig = {
  API_BASE_URL: 'https://api-dev.example.com/v1', // Replace with actual dev API URL
  API_TOKEN_STORAGE_KEY: '@mobileApp:authToken',
  LOCAL_DB_ENCRYPTION_KEY_ALIAS: 'com.thesss.platform.mobile.dbEncryptionKey',
  FEATURE_FLAG_NEW_MAP_UI: true,
};

// Example: Production environment configuration
const prodEnvironment: EnvironmentConfig = {
  API_BASE_URL: 'https://api.example.com/v1', // Replace with actual prod API URL
  API_TOKEN_STORAGE_KEY: '@mobileApp:authToken',
  LOCAL_DB_ENCRYPTION_KEY_ALIAS: 'com.thesss.platform.mobile.dbEncryptionKey',
  FEATURE_FLAG_NEW_MAP_UI: false,
};

// Select environment based on a variable (e.g., __DEV__ in React Native)
// Or use a build-time environment variable injection method
const currentEnvironment = __DEV__ ? devEnvironment : prodEnvironment;

export default currentEnvironment;