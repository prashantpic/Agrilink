import React from 'react';
import DashboardLayout from '../shared/components/templates/DashboardLayout';
import FarmPlotAdminDashboard from '../features/dashboard/farmPlotAdmin/FarmPlotAdminDashboard';
import useDashboardData from '../features/dashboard/common/hooks/useDashboardData';
import { farmPlotAdminDashboardApiService } from '../features/dashboard/farmPlotAdmin/services/farmPlotAdminDashboardApiService';
import { FpaDashboardData } from '../features/dashboard/farmPlotAdmin/types/farmPlotAdminDashboardTypes'; // Assuming this type exists
import { CircularProgress, Typography, Box } from '@mui/material';

const FarmPlotAdminDashboardPage: React.FC = () => {
  const {
    data: fpaData,
    isLoading,
    isError,
    error,
  } = useDashboardData<FpaDashboardData | undefined>(
    ['farmPlotAdminDashboardData'],
    farmPlotAdminDashboardApiService.getFpaDashboardData
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
          Error loading Farm Plot Admin dashboard data: {error instanceof Error ? error.message : 'Unknown error'}
        </Typography>
      )}
      {!isLoading && !isError && fpaData && (
         <FarmPlotAdminDashboard dashboardData={fpaData} />
      )}
      {!isLoading && !isError && !fpaData && (
        <Typography>No data available for the Farm Plot Admin dashboard.</Typography>
      )}
    </DashboardLayout>
  );
};

export default FarmPlotAdminDashboardPage;