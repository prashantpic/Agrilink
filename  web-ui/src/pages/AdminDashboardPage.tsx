import React from 'react';
import DashboardLayout from '../shared/components/templates/DashboardLayout';
import AdminDashboard from '../features/dashboard/admin/AdminDashboard';
import useDashboardData from '../features/dashboard/common/hooks/useDashboardData';
import { adminDashboardApiService } from '../features/dashboard/admin/services/adminDashboardApiService';
import { AdminDashboardData } from '../features/dashboard/admin/types/adminDashboardTypes'; // Assuming this type exists
import { CircularProgress, Typography, Box } from '@mui/material';

const AdminDashboardPage: React.FC = () => {
  const {
    data: adminData,
    isLoading,
    isError,
    error,
  } = useDashboardData<AdminDashboardData | undefined>(
    ['adminDashboardData'],
    adminDashboardApiService.getAdminDashboardData
  );
  return (
    <DashboardLayout>
      {isLoading && (
        <Box display="flex" justifyContent="center" alignItems="center" height="100%">
          <CircularProgress />
        </Box>
      )}
      {isError && (
        <Typography color="error">
          Error loading admin dashboard data: {error instanceof Error ? error.message : 'Unknown error'}
        </Typography>
      )}
      {!isLoading && !isError && adminData && (
        <AdminDashboard dashboardData={adminData} />
      )}
      {!isLoading && !isError && !adminData && (
        <Typography>No data available for the admin dashboard.</Typography>
      )}
    </DashboardLayout>
  );
};

export default AdminDashboardPage;