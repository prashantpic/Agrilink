```typescript
import React from 'react';
import Box from '@mui/material/Box';
import CssBaseline from '@mui/material/CssBaseline';
import Toolbar from '@mui/material/Toolbar';
import Header from '../organisms/Header';
import Sidebar from '../organisms/Sidebar';

// Define a standard width for the sidebar drawer
const drawerWidth = 240;

interface DashboardLayoutProps {
  children: React.ReactNode;
}

const DashboardLayout: React.FC<DashboardLayoutProps> = ({ children }) => {
  const [mobileOpen, setMobileOpen] = React.useState(false);
  // isClosing helps prevent re-opening the drawer during its closing animation
  const [isClosing, setIsClosing] = React.useState(false);

  const handleDrawerClose = () => {
    setIsClosing(true);
    setMobileOpen(false);
  };

  const handleDrawerTransitionEnd = () => {
    // Reset isClosing after the transition is complete
    setIsClosing(false);
  };

  const handleDrawerToggle = () => {
    // Toggle only if not in the process of closing
    if (!isClosing) {
      setMobileOpen(!mobileOpen);
    }
  };

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <CssBaseline />
      {/* Header (AppBar) component */}
      <Header
        drawerWidth={drawerWidth}
        onDrawerToggle={handleDrawerToggle}
      />
      {/* Sidebar (Drawer) component */}
      <Sidebar
        drawerWidth={drawerWidth}
        mobileOpen={mobileOpen}
        onDrawerClose={handleDrawerClose}
        onDrawerTransitionEnd={handleDrawerTransitionEnd}
      />
      {/* Main content area */}
      <Box
        component="main"
        sx={{
          flexGrow: 1, // Allows the main content to take up remaining space
          p: 3, // Standard padding
          // Adjust width for larger screens when the permanent sidebar is visible
          // This assumes the Sidebar component handles its variant (permanent/temporary)
          // and the Header component adjusts its width accordingly.
          width: { sm: `calc(100% - ${drawerWidth}px)` },
        }}
      >
        {/* Toolbar spacer to prevent content from being hidden behind the fixed AppBar */}
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};

export default DashboardLayout;
```