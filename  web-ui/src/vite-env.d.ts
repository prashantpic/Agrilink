/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_GW_BASE_URL: string;
  readonly VITE_AUTH_TOKEN_STORAGE_KEY: string;
  // Add other environment variables here as needed
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}