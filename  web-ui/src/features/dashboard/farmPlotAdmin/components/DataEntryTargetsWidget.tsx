```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert, Box, LinearProgress } from '@mui/material';
import ChartWidget from '../../../dashboard/common/components/ChartWidget';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { ChartData } from '../../../dashboard/common/types/dashboardTypes';

interface DataEntryTarget {
  id: string;
  metricName: string; // e.g., "New Farmer Registrations", "Land Plots Mapped"
  target: number;
  achieved: number;
  unit?: string;
}

interface DataEntryTargetsWidgetData {
  targets: DataEntryTarget[];
  summaryChart?: ChartData; // Optional overall progress chart
}

interface DataEntryTargetsWidgetProps {
  data?: DataEntryTargetsWidgetData;
  isLoading: boolean;
  error?: Error | null;
}

const DataEntryTargetsWidget: React.FC<DataEntryTargetsWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading data entry targets: {error.message}</Alert>;
  }

  if (!data || (!data.targets || data.targets.length === 0) && !data.summaryChart) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Data Entry Targets
          </Typography>
          <Alert severity="info">No data entry targets configured or available.</Alert>
        </CardContent>
      </Card>
    );
  }
  
  const calculateProgress = (achieved: number, target: number) => {
    if (target === 0) return 100; // Avoid division by zero, or handle as appropriate
    return Math.min((achieved / target) * 100, 100);
  };

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Data Entry Targets
        </Typography>
        <Grid container spacing={3}>
          {data.targets && data.targets.map((targetItem) => (
            <Grid item xs={12} sm={6} key={targetItem.id}>
              <Box>
                <Typography variant="subtitle1">{targetItem.metricName}</Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                    <Box sx={{ width: '100%', mr: 1 }}>
                        <LinearProgress variant="determinate" value={calculateProgress(targetItem.achieved, targetItem.target)} />
                    </Box>
                    <Box sx={{ minWidth: 35 }}>
                        <Typography variant="body2" color="text.secondary">{`${Math.round(calculateProgress(targetItem.achieved, targetItem.target))}%`}</Typography>
                    </Box>
                </Box>
                <Typography variant="caption" color="text.secondary">
                    {targetItem.achieved}{targetItem.unit || ''} / {targetItem.target}{targetItem.unit || ''}
                </Typography>
              </Box>
            </Grid>
          ))}
          {data.summaryChart && (
             <Grid item xs={12}>
                <Typography variant="subtitle1" gutterBottom component="div" sx={{mt:1}}>
                    Overall Progress
                </Typography>
                <Box sx={{ height: 250 }}>
                    <ChartWidget
                        type={data.summaryChart.chartType || 'bar'}
                        data={{ labels: data.summaryChart.labels, datasets: data.summaryChart.datasets }}
                        options={data.summaryChart.options || { responsive: true, maintainAspectRatio: false }}
                    />
                </Box>
            </Grid>
          )}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default DataEntryTargetsWidget;
```