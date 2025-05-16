import React from 'react';
import { Grid, Typography, Box, Paper } from '@mui/material';
import DateRangePicker from '../common/components/DateRangePicker';
import ExportButton from '../common/components/ExportButton';
import { DateRange } from '../common/types/dashboardTypes';
// Import specific Admin dashboard widgets here once they are created
// e.g., import SystemStatsWidget from './components/SystemStatsWidget';
// ... and so on for REQ-DASH-009 to REQ-DASH-015

// Placeholder for AdminDashboardData type
interface AdminDashboardData {
  systemStats?: any;
  userRegistrationTrend?: any;
  querySystemOverview?: any;
  apiUsageStats?: any;
  systemHealth?: any;
  dataQualityMetrics?: any;
  masterDataLogs?: any[];
  // Add other relevant data slices for export
}

interface AdminDashboardProps {
  data?: AdminDashboardData; // Data passed from AdminDashboardPage
  isLoading: boolean;
  error?: Error | null;
  dateRange: DateRange;
  onDateRangeChange: (dateRange: DateRange) => void;
}

const AdminDashboard: React.FC<AdminDashboardProps> = ({
  data,
  isLoading,
  error,
  dateRange,
  onDateRangeChange,
}) => {
  if (isLoading) {
    return <Typography>Loading Admin Dashboard...</Typography>;
  }

  if (error) {
    return <Typography color="error">Error loading dashboard: {error.message}</Typography>;
  }

  // Data for export will be the `data` prop, which should be structured by AdminDashboardPage
  // to contain all necessary information from the widgets.
  const exportableData = data;


  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap' }}>
        <Typography variant="h4" gutterBottom component="div">
          Admin Dashboard
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
          <DateRangePicker initialDateRange={dateRange} onDateRangeChange={onDateRangeChange} />
          <ExportButton data={exportableData} fileNamePrefix="admin-dashboard" disabled={!exportableData || isLoading} />
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* REQ-DASH-009: SystemStatsWidget */}
        <Grid item xs={12} md={6} lg={4}>
           <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">System Stats</Typography>
            {data?.systemStats ? <pre>{JSON.stringify(data.systemStats, null, 2)}</pre> : 'SystemStatsWidget Placeholder'}
            {/* <SystemStatsWidget data={data?.systemStats} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-010: UserRegistrationTrendWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">User Registration Trend</Typography>
            {data?.userRegistrationTrend ? <pre>{JSON.stringify(data.userRegistrationTrend, null, 2)}</pre> : 'UserRegistrationTrendWidget Placeholder'}
            {/* <UserRegistrationTrendWidget data={data?.userRegistrationTrend} /> */}
          </Paper>
        </Grid>
        
        {/* REQ-DASH-011: QuerySystemOverviewWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Query System Overview</Typography>
            {data?.querySystemOverview ? <pre>{JSON.stringify(data.querySystemOverview, null, 2)}</pre> : 'QuerySystemOverviewWidget Placeholder'}
            {/* <QuerySystemOverviewWidget data={data?.querySystemOverview} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-012: ApiUsageStatsWidget */}
        <Grid item xs={12} md={6} lg={6}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">API Usage Stats</Typography>
            {data?.apiUsageStats ? <pre>{JSON.stringify(data.apiUsageStats, null, 2)}</pre> : 'ApiUsageStatsWidget Placeholder'}
            {/* <ApiUsageStatsWidget data={data?.apiUsageStats} /> */}
          </Paper>
        </Grid>
        
        {/* REQ-DASH-013: SystemHealthWidget */}
        <Grid item xs={12} md={6} lg={6}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">System Health</Typography>
            {data?.systemHealth ? <pre>{JSON.stringify(data.systemHealth, null, 2)}</pre> : 'SystemHealthWidget Placeholder'}
            {/* <SystemHealthWidget data={data?.systemHealth} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-014: DataQualityMetricsWidget */}
        <Grid item xs={12} md={6} lg={6}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Data Quality Metrics</Typography>
            {data?.dataQualityMetrics ? <pre>{JSON.stringify(data.dataQualityMetrics, null, 2)}</pre> : 'DataQualityMetricsWidget Placeholder'}
            {/* <DataQualityMetricsWidget data={data?.dataQualityMetrics} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-015: MasterDataLogsWidget */}
        <Grid item xs={12} md={6} lg={6}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Master Data Logs</Typography>
            {data?.masterDataLogs ? <pre>{JSON.stringify(data.masterDataLogs, null, 2)}</pre> : 'MasterDataLogsWidget Placeholder'}
            {/* <MasterDataLogsWidget data={data?.masterDataLogs} /> */}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default AdminDashboard;