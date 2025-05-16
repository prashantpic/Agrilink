```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert } from '@mui/material';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { KpiData } from '../../../dashboard/common/types/dashboardTypes';

interface PerformanceMetric {
  label: string; // e.g., "Avg. First Response Time", "Avg. Resolution Time"
  value: string; // Can be string like "X hours" or "Y days"
  unit?: string; // if value is number
  trend?: 'up' | 'down' | 'neutral'; // 'up' might be bad for time metrics
}

interface ConsultantPerformanceWidgetProps {
  data?: PerformanceMetric[];
  isLoading: boolean;
  error?: Error | null;
}

const ConsultantPerformanceWidget: React.FC<ConsultantPerformanceWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading performance metrics: {error.message}</Alert>;
  }

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            My Performance
          </Typography>
          <Alert severity="info">No performance data available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          My Performance
        </Typography>
        <Grid container spacing={2}>
          {data.map((metric, index) => (
            <Grid item xs={12} sm={data.length > 1 ? 6 : 12} key={index}>
              <KpiCard
                label={metric.label}
                value={metric.value}
                unit={metric.unit}
                trend={metric.trend}
              />
            </Grid>
          ))}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default ConsultantPerformanceWidget;
```