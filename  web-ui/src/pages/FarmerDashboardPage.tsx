import React from 'react';
import DashboardLayout from '../shared/components/templates/DashboardLayout';
import FarmerDashboard from '../features/dashboard/farmer/FarmerDashboard';
import useDashboardData from '../features/dashboard/common/hooks/useDashboardData';
import { farmerDashboardApiService } from '../features/dashboard/farmer/services/farmerDashboardApiService';
import { FarmerDashboardData } from '../features/dashboard/farmer/types/farmerDashboardTypes'; // Assuming this type exists
import { CircularProgress, Typography, Box } from '@mui/material';

const FarmerDashboardPage: React.FC = () => {
  const {
    data: farmerData,
    isLoading,
    isError,
    error,
  } = useDashboardData<FarmerDashboardData | undefined>( // Allow undefined for initial state or if API can return no data
    ['farmerDashboardData'],
    farmerDashboardApiService.getFarmerDashboardData
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
          Error loading farmer dashboard data: {error instanceof Error ? error.message : 'Unknown error'}
        </Typography>
      )}
      {!isLoading && !isError && farmerData && (
        <FarmerDashboard dashboardData={farmerData} />
      )}
       {!isLoading && !isError && !farmerData && (
        <Typography>No data available for the farmer dashboard.</Typography>
      )}
    </DashboardLayout>
  );
};

export default FarmerDashboardPage;