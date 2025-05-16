export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH_TOKEN: '/auth/refresh-token', // Example
    ME: '/auth/me', // Example: to get current user info
  },
  DASHBOARD: {
    FARMER: '/dashboard/farmer',
    ADMIN: '/dashboard/admin',
    FPA: '/dashboard/fpa', // Farm Plot Admin
    CONSULTANT: '/dashboard/consultant',
    // Specific widget endpoints could be added here if not aggregated
    // e.g., FARMER_LAND_SUMMARY: '/dashboard/farmer/land-summary'
  },
  FARMER: {
    PROFILE: '/farmers/profile', // Example
    LANDS: '/farmers/lands', // Example
  },
  ADMIN: {
    USERS: '/admin/users', // Example
    SETTINGS: '/admin/settings', // Example
  },
  // ... other service endpoints like Land, Crop, KnowledgeBase, etc.
};