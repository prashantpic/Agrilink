import React from 'react';
import { Grid, Typography, Box, Paper } from '@mui/material';
// Import specific Farmer dashboard widgets here once they are created
// e.g., import LandSummaryWidget from './components/LandSummaryWidget';
// e.g., import ActiveCropsWidget from './components/ActiveCropsWidget';
// ... and so on for REQ-DASH-002 to REQ-DASH-008

// Assuming FarmerDashboardData type will be defined in farmerDashboardTypes.ts
// For now, using a generic placeholder
interface FarmerDashboardData {
  landSummary?: any;
  activeCrops?: any[];
  weatherSnippet?: any;
  recentQueries?: any[];
  farmerAlerts?: any[];
  harvestKpis?: any;
  knowledgeBaseLinks?: any[];
}

interface FarmerDashboardProps {
  data?: FarmerDashboardData; // Data passed from FarmerDashboardPage
  isLoading: boolean;
  error?: Error | null;
}

const FarmerDashboard: React.FC<FarmerDashboardProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <Typography>Loading Farmer Dashboard...</Typography>;
  }

  if (error) {
    return <Typography color="error">Error loading dashboard: {error.message}</Typography>;
  }

  if (!data) {
    return <Typography>No data available for the Farmer Dashboard.</Typography>;
  }

  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Typography variant="h4" gutterBottom component="div" sx={{ mb: 3 }}>
        Farmer Dashboard
      </Typography>
      <Grid container spacing={3}>
        {/* REQ-DASH-002: LandSummaryWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Land Summary</Typography>
            {data.landSummary ? (
              <pre>{JSON.stringify(data.landSummary, null, 2)}</pre>
            ) : (
              <Typography>LandSummaryWidget Placeholder</Typography>
            )}
            {/* <LandSummaryWidget data={data.landSummary} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-003: ActiveCropsWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Active Crops</Typography>
             {data.activeCrops ? (
              <pre>{JSON.stringify(data.activeCrops, null, 2)}</pre>
            ) : (
              <Typography>ActiveCropsWidget Placeholder</Typography>
            )}
            {/* <ActiveCropsWidget data={data.activeCrops} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-004: FarmerWeatherSnippetWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Weather Snippet</Typography>
             {data.weatherSnippet ? (
              <pre>{JSON.stringify(data.weatherSnippet, null, 2)}</pre>
            ) : (
              <Typography>FarmerWeatherSnippetWidget Placeholder</Typography>
            )}
            {/* <FarmerWeatherSnippetWidget data={data.weatherSnippet} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-005: RecentQueriesWidget */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Recent Queries</Typography>
             {data.recentQueries ? (
              <pre>{JSON.stringify(data.recentQueries, null, 2)}</pre>
            ) : (
              <Typography>RecentQueriesWidget Placeholder</Typography>
            )}
            {/* <RecentQueriesWidget data={data.recentQueries} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-006: FarmerAlertsWidget */}
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Alerts</Typography>
             {data.farmerAlerts ? (
              <pre>{JSON.stringify(data.farmerAlerts, null, 2)}</pre>
            ) : (
              <Typography>FarmerAlertsWidget Placeholder</Typography>
            )}
            {/* <FarmerAlertsWidget data={data.farmerAlerts} /> */}
          </Paper>
        </Grid>
        
        {/* REQ-DASH-007: HarvestKpiWidget */}
        <Grid item xs={12} md={6} lg={4}>
          <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Harvest KPIs</Typography>
             {data.harvestKpis ? (
              <pre>{JSON.stringify(data.harvestKpis, null, 2)}</pre>
            ) : (
              <Typography>HarvestKpiWidget Placeholder</Typography>
            )}
            {/* <HarvestKpiWidget data={data.harvestKpis} /> */}
          </Paper>
        </Grid>

        {/* REQ-DASH-008: KnowledgeBaseLinksWidget */}
        <Grid item xs={12} md={6} lg={8}>
           <Paper sx={{ p: 2, textAlign: 'center', height: '100%' }}>
            <Typography variant="h6">Knowledge Base Links</Typography>
             {data.knowledgeBaseLinks ? (
              <pre>{JSON.stringify(data.knowledgeBaseLinks, null, 2)}</pre>
            ) : (
              <Typography>KnowledgeBaseLinksWidget Placeholder</Typography>
            )}
            {/* <KnowledgeBaseLinksWidget data={data.knowledgeBaseLinks} /> */}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default FarmerDashboard;