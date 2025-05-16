interface AppConfig {
  API_GW_BASE_URL: string;
  AUTH_TOKEN_STORAGE_KEY: string;
  NODE_ENV: string;
  // Feature Flags
  ENABLE_NEW_ADMIN_DASHBOARD_WIDGETS: boolean;
  SHOW_EXPERIMENTAL_FARMER_FEATURES: boolean;
  ENABLE_ADVANCED_EXPORT_OPTIONS: boolean;
}

export const appConfig: AppConfig = {
  API_GW_BASE_URL: import.meta.env.VITE_API_GW_BASE_URL || 'http://localhost:8080/api/v1',
  AUTH_TOKEN_STORAGE_KEY: import.meta.env.VITE_AUTH_TOKEN_STORAGE_KEY || 'appAuthToken',
  NODE_ENV: import.meta.env.MODE || 'development',

  // Feature flags - parse from env vars, defaulting to false
  ENABLE_NEW_ADMIN_DASHBOARD_WIDGETS: import.meta.env.VITE_ENABLE_NEW_ADMIN_DASHBOARD_WIDGETS === 'true',
  SHOW_EXPERIMENTAL_FARMER_FEATURES: import.meta.env.VITE_SHOW_EXPERIMENTAL_FARMER_FEATURES === 'true',
  ENABLE_ADVANCED_EXPORT_OPTIONS: import.meta.env.VITE_ENABLE_ADVANCED_EXPORT_OPTIONS === 'true',
};