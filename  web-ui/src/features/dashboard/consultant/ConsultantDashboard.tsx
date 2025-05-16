import React from 'react';
import { Grid, Typography, Box, Paper } from '@mui/material';
import DateRangePicker from '../common/components/DateRangePicker';
import ExportButton from '../common/components/ExportButton';
import { DateRange } from '../common/types/dashboardTypes';
// Import specific Consultant dashboard widgets here once they are created
// e.g., import ConsultantQueryLoadWidget from './components/ConsultantQueryLoadWidget';
// ... and so on for REQ-DASH-020 to REQ-DASH-024

// Placeholder for ConsultantDashboardData type
interface ConsultantDashboardData {
  queryLoad?: any;
  consultantPerformance?: any;
  farmerFeedbackSummary?: any;
  recentAnswers?: any[];
  kbContributions?: any;
  // Add other relevant data slices for export
}

interface ConsultantDashboardProps {
  data?: ConsultantDashboardData; // Data passed from ConsultantDashboardPage
  isLoading: boolean;
  error?: Error | null;
  dateRange: DateRange;
  onDateRangeChange: (dateRange: DateRange) => void;
}

const ConsultantDashboard: React.FC<ConsultantDashboardProps> = ({
  data,
  isLoading,
  error,
  dateRange,
  onDateRangeChange,
}) => {
  if (isLoading) {
    return <Typography>Loading Consultant Dashboard...</Typography>;
  }

  if (error) {
    return <Typography color="error">Error loading dashboard: {error.message}</Typography>;
  }
  
  const exportableData = data;

  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap' }}>
        <Typography variant="h4" gutterBottom component="div">
          Consultant Dashboard
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', flexWrap: 'wrap' }}>
          <DateRangePicker initialDateRange={dateRange} onDateRangeChange={onDateRangeChange} />
          <ExportButton data={exportableData} fileNamePrefix="consultant-dashboard" disabled={!exportableData || isLoading}/>
        </Box>
      </Box>

      <Grid container spacing={3}>
        {/* REQ-DASH-020: ConsultantQueryLoadWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Query Load</Typography>
            {data?.queryLoad ? <pre>{JSON.stringify(data.queryLoad, null, 2)}</pre> : 'ConsultantQueryLoadWidget Placeholder'}
            {/* <ConsultantQueryLoadWidget data={data?.queryLoad} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-021: ConsultantPerformanceWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Performance Metrics</Typography>
            {data?.consultantPerformance ? <pre>{JSON.stringify(data.consultantPerformance, null, 2)}</pre> : 'ConsultantPerformanceWidget Placeholder'}
            {/* <ConsultantPerformanceWidget data={data?.consultantPerformance} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-022: FarmerFeedbackSummaryWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Farmer Feedback Summary</Typography>
            {data?.farmerFeedbackSummary ? <pre>{JSON.stringify(data.farmerFeedbackSummary, null, 2)}</pre> : 'FarmerFeedbackSummaryWidget Placeholder'}
            {/* <FarmerFeedbackSummaryWidget data={data?.farmerFeedbackSummary} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-023: RecentAnswersWidget */}
        <Grid item xs={12} md={12} lg={8}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Recent Answers</Typography>
            {data?.recentAnswers ? <pre>{JSON.stringify(data.recentAnswers, null, 2)}</pre> : 'RecentAnswersWidget Placeholder'}
            {/* <RecentAnswersWidget data={data?.recentAnswers} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-024: KbContributionsWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">KB Contributions</Typography>
            {data?.kbContributions ? <pre>{JSON.stringify(data.kbContributions, null, 2)}</pre> : 'KbContributionsWidget Placeholder'}
            {/* <KbContributionsWidget data={data?.kbContributions} /> */}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default ConsultantDashboard;