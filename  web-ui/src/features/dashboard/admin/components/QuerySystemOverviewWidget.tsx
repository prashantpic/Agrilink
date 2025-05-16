```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert, Box } from '@mui/material';
import ChartWidget from '../../../dashboard/common/components/ChartWidget';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { ChartData, KpiData } from '../../../dashboard/common/types/dashboardTypes';

interface ConsultantPerformance {
  id: string;
  name: string;
  resolvedQueries: number;
  avgResolutionTime: string; // e.g., "2.5 days"
}

interface QuerySystemOverviewData {
  totalQueries: KpiData;
  openClosedRatioChart?: ChartData; // e.g., pie chart
  avgResolutionTime: KpiData;
  slaAdherence: KpiData; // e.g., percentage
  consultantLeaderboard?: ConsultantPerformance[];
}

interface QuerySystemOverviewWidgetProps {
  data?: QuerySystemOverviewData;
  isLoading: boolean;
  error?: Error | null;
}

const leaderboardColumns: GridColDef<ConsultantPerformance>[] = [
  { field: 'name', headerName: 'Consultant', flex: 1 },
  { field: 'resolvedQueries', headerName: 'Resolved', type: 'number', flex: 0.5 },
  { field: 'avgResolutionTime', headerName: 'Avg. Resolution Time', flex: 1 },
];

const QuerySystemOverviewWidget: React.FC<QuerySystemOverviewWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading query system overview: {error.message}</Alert>;
  }

  if (!data) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Query System Overview
          </Typography>
          <Alert severity="info">No query system data available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Query System Overview
        </Typography>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6} md={3}>
            <KpiCard label={data.totalQueries.label} value={data.totalQueries.value} unit={data.totalQueries.unit} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <KpiCard label={data.avgResolutionTime.label} value={data.avgResolutionTime.value} unit={data.avgResolutionTime.unit} />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <KpiCard label={data.slaAdherence.label} value={data.slaAdherence.value} unit={data.slaAdherence.unit} />
          </Grid>
          {data.openClosedRatioChart && (
            <Grid item xs={12} sm={6} md={3}>
               <Typography variant="subtitle2" align="center" gutterBottom>Open/Closed Ratio</Typography>
               <Box sx={{ height: 150}}>
                <ChartWidget
                    type={data.openClosedRatioChart.chartType || 'pie'}
                    data={{ labels: data.openClosedRatioChart.labels, datasets: data.openClosedRatioChart.datasets }}
                    options={data.openClosedRatioChart.options || { responsive: true, maintainAspectRatio: false }}
                />
               </Box>
            </Grid>
          )}
          {data.consultantLeaderboard && data.consultantLeaderboard.length > 0 && (
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom component="div" sx={{mt: 2}}>
                Consultant Performance
              </Typography>
              <Box sx={{ height: 250, width: '100%' }}>
                <DataGrid
                  rows={data.consultantLeaderboard}
                  columns={leaderboardColumns}
                  pageSizeOptions={[5]}
                  disableRowSelectionOnClick
                  autoHeight
                />
              </Box>
            </Grid>
          )}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default QuerySystemOverviewWidget;
```