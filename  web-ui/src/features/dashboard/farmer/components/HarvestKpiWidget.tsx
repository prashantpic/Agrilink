```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert } from '@mui/material';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { KpiData } from '../../../dashboard/common/types/dashboardTypes';

interface HarvestKpi {
  label: string;
  value: number | string;
  unit?: string;
  cropName?: string; // Optional: specific crop this KPI refers to
  trend?: 'up' | 'down' | 'neutral';
}

interface HarvestKpiWidgetProps {
  data?: HarvestKpi[];
  isLoading: boolean;
  error?: Error | null;
}

const HarvestKpiWidget: React.FC<HarvestKpiWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading harvest KPIs: {error.message}</Alert>;
  }

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Recent Harvest KPIs
          </Typography>
          <Alert severity="info">No recent harvest KPIs available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Recent Harvest KPIs
        </Typography>
        <Grid container spacing={2}>
          {data.map((kpi, index) => (
            <Grid item xs={12} sm={data.length > 1 ? 6 : 12} md={data.length > 2 ? 4 : (data.length > 1 ? 6: 12)} key={index}>
              <KpiCard
                label={kpi.cropName ? `${kpi.label} (${kpi.cropName})` : kpi.label}
                value={kpi.value}
                unit={kpi.unit}
                trend={kpi.trend}
              />
            </Grid>
          ))}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default HarvestKpiWidget;
```