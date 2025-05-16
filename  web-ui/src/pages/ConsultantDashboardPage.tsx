import React from 'react';
import DashboardLayout from '../shared/components/templates/DashboardLayout';
import ConsultantDashboard from '../features/dashboard/consultant/ConsultantDashboard';
import useDashboardData from '../features/dashboard/common/hooks/useDashboardData';
import { consultantDashboardApiService } from '../features/dashboard/consultant/services/consultantDashboardApiService';
import { ConsultantDashboardData } from '../features/dashboard/consultant/types/consultantDashboardTypes'; // Assuming this type exists
import { CircularProgress, Typography, Box } from '@mui/material';

const ConsultantDashboardPage: React.FC = () => {
  const {
    data: consultantData,
    isLoading,
    isError,
    error,
  } = useDashboardData<ConsultantDashboardData | undefined>(
    ['consultantDashboardData'],
    consultantDashboardApiService.getConsultantDashboardData
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
          Error loading consultant dashboard data: {error instanceof Error ? error.message : 'Unknown error'}
        </Typography>
      )}
      {!isLoading && !isError && consultantData && (
         <ConsultantDashboard dashboardData={consultantData} />
      )}
      {!isLoading && !isError && !consultantData && (
        <Typography>No data available for the consultant dashboard.</Typography>
      )}
    </DashboardLayout>
  );
};

export default ConsultantDashboardPage;