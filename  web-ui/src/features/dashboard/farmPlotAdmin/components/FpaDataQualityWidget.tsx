```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert, Box } from '@mui/material';
import ChartWidget from '../../../dashboard/common/components/ChartWidget';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { ChartData, KpiData } from '../../../dashboard/common/types/dashboardTypes';

interface FpaDataQualityMetric {
  id: string;
  label: string; // e.g., "Farmers with complete profiles", "Land with GPS data"
  value: number; // percentage
  description?: string;
  totalCount?: number; // e.g. total farmers this FPA manages
  compliantCount?: number; // e.g. farmers with complete profiles
}

interface FpaDataQualityWidgetData {
  metrics: FpaDataQualityMetric[];
  overallQualityChart?: ChartData; // e.g., a bar chart showing different metrics
}

interface FpaDataQualityWidgetProps {
  data?: FpaDataQualityWidgetData;
  isLoading: boolean;
  error?: Error | null;
}

const FpaDataQualityWidget: React.FC<FpaDataQualityWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading FPA data quality metrics: {error.message}</Alert>;
  }

  if (!data || (!data.metrics || data.metrics.length === 0) && !data.overallQualityChart) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Data Quality (Managed Records)
          </Typography>
          <Alert severity="info">No data quality metrics available for your records.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Data Quality (Managed Records)
        </Typography>
        <Grid container spacing={2}>
          {data.metrics && data.metrics.map((metric) => (
            <Grid item xs={12} sm={6} key={metric.id}>
              <KpiCard
                label={metric.label}
                value={metric.value}
                unit="%"
                description={metric.compliantCount !== undefined && metric.totalCount !== undefined ? `${metric.compliantCount} / ${metric.totalCount}` : metric.description}
              />
            </Grid>
          ))}
          {data.overallQualityChart && (
             <Grid item xs={12}>
                <Typography variant="subtitle1" gutterBottom component="div" sx={{mt:1}}>
                    Quality Overview
                </Typography>
                <Box sx={{ height: 250 }}>
                    <ChartWidget
                        type={data.overallQualityChart.chartType || 'bar'}
                        data={{ labels: data.overallQualityChart.labels, datasets: data.overallQualityChart.datasets }}
                        options={data.overallQualityChart.options || { responsive: true, maintainAspectRatio: false }}
                    />
                </Box>
            </Grid>
          )}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default FpaDataQualityWidget;
```