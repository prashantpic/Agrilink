import React from 'react';
import LoginForm from '../features/auth/components/LoginForm';
import { Container, Box, Typography, Paper } from '@mui/material';

const LoginPage: React.FC = () => {
  return (
    <Container component="main" maxWidth="xs">
      <Paper elevation={3} sx={{ marginTop: 8, padding: 4, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <Typography component="h1" variant="h5" sx={{ mb: 2 }}>
          Sign In
        </Typography>
        <LoginForm />
      </Paper>
      <Box mt={5} textAlign="center">
        <Typography variant="body2" color="text.secondary">
          {'Copyright © Your Company '}
          {new Date().getFullYear()}
          {'.'}
        </Typography>
      </Box>
    </Container>
  );
};

export default LoginPage;