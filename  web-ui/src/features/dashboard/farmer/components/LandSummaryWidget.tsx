```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert } from '@mui/material';
import KpiCard from '../../../dashboard/common/components/KpiCard'; // Assuming KpiCard is in common
import { KpiData } from '../../../dashboard/common/types/dashboardTypes'; // Assuming KpiData is in common

// Define a more specific type for the data this widget expects
interface LandSummaryData {
  totalArea: number;
  areaUnit: string;
  plotCount: number;
}

interface LandSummaryWidgetProps {
  data?: LandSummaryData;
  isLoading: boolean;
  error?: Error | null;
}

const LandSummaryWidget: React.FC<LandSummaryWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading land summary: {error.message}</Alert>;
  }

  if (!data) {
    return <Alert severity="info">No land summary data available.</Alert>;
  }

  const kpis: KpiData[] = [
    {
      label: 'Total Land Area',
      value: data.totalArea,
      unit: data.areaUnit,
    },
    {
      label: 'Number of Plots',
      value: data.plotCount,
    },
  ];

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Land Summary
        </Typography>
        <Grid container spacing={2}>
          {kpis.map((kpi) => (
            <Grid item xs={12} sm={6} key={kpi.label}>
              <KpiCard
                label={kpi.label}
                value={kpi.value}
                unit={kpi.unit}
              />
            </Grid>
          ))}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default LandSummaryWidget;
```