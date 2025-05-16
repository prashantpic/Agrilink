```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert } from '@mui/material';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { KpiData } from '../../../dashboard/common/types/dashboardTypes';

interface QueryLoadStat {
  label: string; // e.g., "Total Open Queries", "Assigned to You", "Awaiting Your Action"
  value: number;
}

interface ConsultantQueryLoadWidgetProps {
  data?: QueryLoadStat[];
  isLoading: boolean;
  error?: Error | null;
}

const ConsultantQueryLoadWidget: React.FC<ConsultantQueryLoadWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading query load statistics: {error.message}</Alert>;
  }

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Query Load
          </Typography>
          <Alert severity="info">No query load data available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Query Load
        </Typography>
        <Grid container spacing={2}>
          {data.map((stat, index) => (
            <Grid item xs={12} sm={data.length > 1 ? 6 : 12} md={data.length > 2 ? 4 : (data.length > 1 ? 6:12)} key={index}>
              <KpiCard
                label={stat.label}
                value={stat.value}
              />
            </Grid>
          ))}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default ConsultantQueryLoadWidget;
```