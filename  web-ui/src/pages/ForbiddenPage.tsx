import React from 'react';
import { Box, Typography, Button, Container } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { routePaths } from '../core/routing/routePaths';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';

const ForbiddenPage: React.FC = () => {
  return (
    <Container component="main" maxWidth="sm" sx={{ textAlign: 'center', mt: 8 }}>
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: 'calc(100vh - 200px)', // Adjust as needed
        }}
      >
        <LockOutlinedIcon sx={{ fontSize: 80, color: 'error.main', mb: 2 }} />
        <Typography variant="h1" component="h1" gutterBottom sx={{ fontWeight: 'bold', color: 'error.main' }}>
          403
        </Typography>
        <Typography variant="h5" component="h2" gutterBottom>
          Access Denied / Forbidden
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          You do not have permission to view this page. Please contact your administrator if you believe this is an error.
        </Typography>
        <Button
          variant="contained"
          component={RouterLink}
          to={routePaths.LOGIN} // Or a generic home page
          size="large"
        >
          Go to Homepage
        </Button>
      </Box>
    </Container>
  );
};

export default ForbiddenPage;