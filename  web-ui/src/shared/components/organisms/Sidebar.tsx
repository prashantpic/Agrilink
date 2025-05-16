```typescript
import React from 'react';
import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Toolbar, Divider, Box } from '@mui/material';
import { NavLink as RouterLink, useLocation } from 'react-router-dom';
import DashboardIcon from '@mui/icons-material/Dashboard';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import PeopleIcon from '@mui/icons-material/People';
import GrassIcon from '@mui/icons-material/Grass'; // Farmer
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd'; // Consultant
import AssessmentIcon from '@mui/icons-material/Assessment'; // Farm Plot Admin

import { useAuth } from '../../../features/auth/hooks/useAuth';
import { roleConfig } from '../../../core/config/roleConfig';
import { routePaths } from '../../../core/routing/routePaths';
import { UserRole } from '../../../types/user'; // Assuming UserRole type

const drawerWidth = 240;

interface NavItem {
  text: string;
  path: string;
  icon: React.ReactElement;
  allowedRoles: UserRole[];
}

const navItems: NavItem[] = [
  {
    text: 'Farmer Dashboard',
    path: routePaths.DASHBOARD_FARMER,
    icon: <GrassIcon />,
    allowedRoles: [roleConfig.ROLES.FARMER],
  },
  {
    text: 'Admin Dashboard',
    path: routePaths.DASHBOARD_ADMIN,
    icon: <AdminPanelSettingsIcon />,
    allowedRoles: [roleConfig.ROLES.ADMIN],
  },
  {
    text: 'FPA Dashboard', // Farm Plot Admin
    path: routePaths.DASHBOARD_FPA,
    icon: <AssessmentIcon />,
    allowedRoles: [roleConfig.ROLES.FARM_PLOT_ADMIN],
  },
  {
    text: 'Consultant Dashboard',
    path: routePaths.DASHBOARD_CONSULTANT,
    icon: <AssignmentIndIcon />,
    allowedRoles: [roleConfig.ROLES.CONSULTANT],
  },
  // Add other common navigation items here if any
  // e.g., User Management for Admin
  // {
  //   text: 'User Management',
  //   path: routePaths.USER_MANAGEMENT, // Define this path
  //   icon: <PeopleIcon />,
  //   allowedRoles: [roleConfig.ROLES.ADMIN],
  // }
];

interface SidebarProps {
  mobileOpen?: boolean;
  onDrawerToggle?: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ mobileOpen, onDrawerToggle }) => {
  const { user } = useAuth();
  const location = useLocation();

  const drawerContent = (
    <div>
      <Toolbar /> {/* For spacing under the AppBar */}
      <Divider />
      <List>
        {navItems.map((item) =>
          item.allowedRoles.includes(user?.role as UserRole) ? ( // Type assertion might be needed
            <ListItem key={item.text} disablePadding component={RouterLink} to={item.path} sx={{
                color: 'inherit',
                textDecoration: 'none',
                '&.active': {
                    backgroundColor: (theme) => theme.palette.action.selected,
                    color: (theme) => theme.palette.primary.main,
                    '& .MuiListItemIcon-root': {
                        color: (theme) => theme.palette.primary.main,
                    }
                },
            }}
            className={location.pathname === item.path ? 'active' : ''}
            >
              <ListItemButton selected={location.pathname === item.path}>
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={item.text} />
              </ListItemButton>
            </ListItem>
          ) : null
        )}
      </List>
    </div>
  );

  return (
    <Box
      component="nav"
      sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      aria-label="mailbox folders"
    >
      {/* Mobile Drawer */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={onDrawerToggle}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile.
        }}
        sx={{
          display: { xs: 'block', sm: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
      >
        {drawerContent}
      </Drawer>
      {/* Desktop Drawer */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', sm: 'block' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
        open
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
};

export default Sidebar;
```