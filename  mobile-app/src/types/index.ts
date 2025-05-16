// This is a barrel file for re-exporting all type definitions.
// As other type definition files (e.g., auth.ts, user.ts, farmer.ts, sync.ts)
// are created in this directory, they should be exported from here.

export * from './sync'; // Assuming sync.ts will contain sync-related types (REQ-14-004, REQ-14-008)
// export * from './auth';
// export * from './user';
// export * from './farmer';
// export * from './land';
// ... and so on for other domain or feature-specific types.

// Example of a generic type that might be used across the app
export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  errorCode?: string;
}

export interface UserProfile {
    id: string;
    username: string;
    email: string;
    role: string; // e.g., 'FARMER', 'ADMIN', 'FIELD_AGENT'
    // other profile fields
}