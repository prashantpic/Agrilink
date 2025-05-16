import React from 'react';
import { Grid, Typography, Box, Paper } from '@mui/material';
// Import specific FarmPlotAdmin dashboard widgets here once they are created
// e.g., import DataEntryActivityWidget from './components/DataEntryActivityWidget';
// ... and so on for REQ-DASH-016 to REQ-DASH-019

// Placeholder for FpaDashboardData type
interface FpaDashboardData {
  dataEntryActivity?: any;
  pendingTasks?: any[];
  fpaDataQuality?: any;
  dataEntryTargets?: any;
}

interface FarmPlotAdminDashboardProps {
  data?: FpaDashboardData; // Data passed from FarmPlotAdminDashboardPage
  isLoading: boolean;
  error?: Error | null;
}

const FarmPlotAdminDashboard: React.FC<FarmPlotAdminDashboardProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <Typography>Loading Farm Plot Admin Dashboard...</Typography>;
  }

  if (error) {
    return <Typography color="error">Error loading dashboard: {error.message}</Typography>;
  }

  if (!data) {
    return <Typography>No data available for the Farm Plot Admin Dashboard.</Typography>;
  }

  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Typography variant="h4" gutterBottom component="div" sx={{ mb: 3 }}>
        Farm Plot Admin Dashboard
      </Typography>
      <Grid container spacing={3}>
        {/* REQ-DASH-016: DataEntryActivityWidget */}
        <Grid item xs={12} md={6} lg={3}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Data Entry Activity</Typography>
            {data.dataEntryActivity ? <pre>{JSON.stringify(data.dataEntryActivity, null, 2)}</pre> : 'DataEntryActivityWidget Placeholder'}
            {/* <DataEntryActivityWidget data={data.dataEntryActivity} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-017: PendingTasksWidget */}
        <Grid item xs={12} md={6} lg={9}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Pending Tasks</Typography>
            {data.pendingTasks ? <pre>{JSON.stringify(data.pendingTasks, null, 2)}</pre> : 'PendingTasksWidget Placeholder'}
            {/* <PendingTasksWidget data={data.pendingTasks} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-018: FpaDataQualityWidget */}
        <Grid item xs={12} md={6} lg={6}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Data Quality</Typography>
            {data.fpaDataQuality ? <pre>{JSON.stringify(data.fpaDataQuality, null, 2)}</pre> : 'FpaDataQualityWidget Placeholder'}
            {/* <FpaDataQualityWidget data={data.fpaDataQuality} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-019: DataEntryTargetsWidget */}
        <Grid item xs={12} md={6} lg={6}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Data Entry Targets</Typography>
            {data.dataEntryTargets ? <pre>{JSON.stringify(data.dataEntryTargets, null, 2)}</pre> : 'DataEntryTargetsWidget Placeholder'}
            {/* <DataEntryTargetsWidget data={data.dataEntryTargets} /> */}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default FarmPlotAdminDashboard;