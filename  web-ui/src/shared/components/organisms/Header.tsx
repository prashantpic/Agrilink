```typescript
import React, { useState } from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton, Avatar, Menu, MenuItem, Box } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu'; // For mobile sidebar toggle, if needed
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../features/auth/hooks/useAuth'; // Assuming useAuth hook
import { routePaths } from '../../../core/routing/routePaths';

interface HeaderProps {
  onDrawerToggle?: () => void; // For mobile sidebar
  appName?: string;
}

const Header: React.FC<HeaderProps> = ({ onDrawerToggle, appName = "Agricultural Platform" }) => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout(); // Dispatch logout action
    handleClose();
    navigate(routePaths.LOGIN); // Redirect to login page
  };

  const handleProfile = () => {
    // navigate(routePaths.PROFILE); // If a profile page exists
    handleClose();
    // For now, just log or do nothing
    console.log("Navigate to profile");
  };

  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        {onDrawerToggle && (
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={onDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
        )}
        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
          {appName}
        </Typography>
        {isAuthenticated && user ? (
          <>
            <IconButton
              size="large"
              aria-label="account of current user"
              aria-controls="menu-appbar"
              aria-haspopup="true"
              onClick={handleMenu}
              color="inherit"
            >
              <Avatar sx={{ bgcolor: 'secondary.main', width: 32, height: 32 }}>
                {user.username ? user.username.charAt(0).toUpperCase() : <AccountCircleIcon />}
              </Avatar>
            </IconButton>
            <Menu
              id="menu-appbar"
              anchorEl={anchorEl}
              anchorOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              keepMounted
              transformOrigin={{
                vertical: 'top',
                horizontal: 'right',
              }}
              open={open}
              onClose={handleClose}
              sx={{mt: '45px'}}
            >
              <MenuItem disabled sx={{ justifyContent: 'center', fontWeight: 'bold' }}>
                {user.username} ({user.role})
              </MenuItem>
              {/* <MenuItem onClick={handleProfile}>Profile</MenuItem> */}
              <MenuItem onClick={handleLogout}>Logout</MenuItem>
            </Menu>
          </>
        ) : (
          <Button color="inherit" onClick={() => navigate(routePaths.LOGIN)}>
            Login
          </Button>
        )}
      </Toolbar>
    </AppBar>
  );
};

export default Header;
```