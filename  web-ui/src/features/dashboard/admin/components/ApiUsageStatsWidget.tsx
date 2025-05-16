```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert, Box } from '@mui/material';
import ChartWidget from '../../../dashboard/common/components/ChartWidget';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { DataGrid, GridColDef } from '@mui/x-data-grid';
import { ChartData, KpiData } from '../../../dashboard/common/types/dashboardTypes';

interface ApiKeyUsage {
  id: string;
  apiKeyAlias: string; // e.g., "Partner X"
  requestCount: number;
  errorRate: number; // percentage
}

interface ApiUsageStatsData {
  totalRequests: KpiData;
  errorRateOverall: KpiData;
  requestsOverTimeChart?: ChartData; // e.g., line chart for volume
  apiKeyUsageSummary?: ApiKeyUsage[];
}

interface ApiUsageStatsWidgetProps {
  data?: ApiUsageStatsData;
  isLoading: boolean;
  error?: Error | null;
}

const apiKeyUsageColumns: GridColDef<ApiKeyUsage>[] = [
  { field: 'apiKeyAlias', headerName: 'API Key User', flex: 1 },
  { field: 'requestCount', headerName: 'Requests', type: 'number', flex: 0.5 },
  { field: 'errorRate', headerName: 'Error Rate (%)', type: 'number', flex: 0.5, valueFormatter: (params) => `${params.value}%` },
];

const ApiUsageStatsWidget: React.FC<ApiUsageStatsWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading API usage statistics: {error.message}</Alert>;
  }

  if (!data) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Public API Usage
          </Typography>
          <Alert severity="info">No API usage data available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Public API Usage
        </Typography>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6}>
            <KpiCard label={data.totalRequests.label} value={data.totalRequests.value} unit={data.totalRequests.unit} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <KpiCard label={data.errorRateOverall.label} value={data.errorRateOverall.value} unit={data.errorRateOverall.unit} />
          </Grid>
          {data.requestsOverTimeChart && (
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom component="div" sx={{mt:1}}>
                Request Volume Over Time
              </Typography>
              <Box sx={{ height: 250 }}>
                <ChartWidget
                    type={data.requestsOverTimeChart.chartType || 'line'}
                    data={{ labels: data.requestsOverTimeChart.labels, datasets: data.requestsOverTimeChart.datasets }}
                    options={data.requestsOverTimeChart.options || { responsive: true, maintainAspectRatio: false }}
                />
              </Box>
            </Grid>
          )}
          {data.apiKeyUsageSummary && data.apiKeyUsageSummary.length > 0 && (
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom component="div" sx={{mt:1}}>
                Usage by API Key
              </Typography>
              <Box sx={{ height: 250, width: '100%' }}>
                <DataGrid
                  rows={data.apiKeyUsageSummary}
                  columns={apiKeyUsageColumns}
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

export default ApiUsageStatsWidget;
```