```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert } from '@mui/material';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { KpiData } from '../../../dashboard/common/types/dashboardTypes';

interface SystemStat {
  label: string;
  value: number | string;
  unit?: string;
}

interface SystemStatsWidgetProps {
  data?: SystemStat[];
  isLoading: boolean;
  error?: Error | null;
}

const SystemStatsWidget: React.FC<SystemStatsWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading system statistics: {error.message}</Alert>;
  }

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            System Statistics
          </Typography>
          <Alert severity="info">No system statistics available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          System Statistics
        </Typography>
        <Grid container spacing={2}>
          {data.map((stat, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}> {/* Adjust grid sizing as needed */}
              <KpiCard
                label={stat.label}
                value={stat.value}
                unit={stat.unit}
              />
            </Grid>
          ))}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default SystemStatsWidget;
```