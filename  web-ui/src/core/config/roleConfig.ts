import { UserRole } from '../../types/user';

// Define roles as an enum or string constants
// This ensures consistency with UserRole type
export const ROLES: Record<string, UserRole> = {
  FARMER: 'Farmer',
  ADMIN: 'Admin',
  FARM_PLOT_ADMIN: 'Farm Plot Admin',
  CONSULTANT: 'Consultant',
};

interface RoleConfig {
  // Example: Define specific permissions or accessible components per role
  // For now, we primarily use the ROLES constants in ProtectedRoute
  // We could extend this to define sidebar links or dashboard widgets per role
  sidebarLinks?: {
    [key in UserRole]?: { path: string; label: string; icon?: React.ElementType }[];
  };
  // Add other role-specific configurations if needed
}

export const roleConfig: RoleConfig = {
  // Example for sidebar link structure (implementation would be in Sidebar.tsx)
  sidebarLinks: {
    [ROLES.FARMER]: [
      // { path: routePaths.DASHBOARD_FARMER, label: 'Dashboard' /* icon: DashboardIcon */ },
      // { path: routePaths.FARMER_LAND_MANAGEMENT, label: 'My Land' /* icon: LandscapeIcon */ },
    ],
    [ROLES.ADMIN]: [
      // { path: routePaths.DASHBOARD_ADMIN, label: 'Admin Dashboard' /* icon: AdminPanelSettingsIcon */ },
      // { path: routePaths.ADMIN_USER_MANAGEMENT, label: 'User Management' /* icon: PeopleIcon */ },
    ],
    // ... other roles
  },
};

// Export ROLES for direct use in ProtectedRoute allowedRoles prop
// e.g. allowedRoles={[ROLES.ADMIN, ROLES.CONSULTANT]}