```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert, Box } from '@mui/material';
import ChartWidget from '../../../dashboard/common/components/ChartWidget';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { ChartData, KpiData } from '../../../dashboard/common/types/dashboardTypes';

interface DataQualityMetric {
  id: string;
  label: string; // e.g., "Farmer Profile Completeness"
  value: number; // percentage
  description?: string;
}

interface DataQualityMetricsData {
  metrics: DataQualityMetric[];
  overallQualityScore?: KpiData; // Optional overall score
  completenessChart?: ChartData; // Optional chart visualising different completeness aspects
}

interface DataQualityMetricsWidgetProps {
  data?: DataQualityMetricsData;
  isLoading: boolean;
  error?: Error | null;
}

const DataQualityMetricsWidget: React.FC<DataQualityMetricsWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading data quality metrics: {error.message}</Alert>;
  }

  if (!data || (!data.metrics || data.metrics.length === 0) && !data.completenessChart) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Data Quality Metrics
          </Typography>
          <Alert severity="info">No data quality metrics available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Data Quality Metrics
        </Typography>
        <Grid container spacing={2}>
          {data.overallQualityScore && (
            <Grid item xs={12}>
                <KpiCard label={data.overallQualityScore.label} value={data.overallQualityScore.value} unit={data.overallQualityScore.unit} />
            </Grid>
          )}
          {data.metrics && data.metrics.map((metric) => (
            <Grid item xs={12} sm={6} md={data.metrics.length > 2 ? 4 : 6} key={metric.id}>
              <KpiCard label={metric.label} value={metric.value} unit="%" description={metric.description} />
            </Grid>
          ))}
          {data.completenessChart && (
             <Grid item xs={12}>
                <Typography variant="subtitle1" gutterBottom component="div" sx={{mt:1}}>
                    Completeness Overview
                </Typography>
                <Box sx={{ height: 250 }}>
                    <ChartWidget
                        type={data.completenessChart.chartType || 'bar'}
                        data={{ labels: data.completenessChart.labels, datasets: data.completenessChart.datasets }}
                        options={data.completenessChart.options || { responsive: true, maintainAspectRatio: false }}
                    />
                </Box>
            </Grid>
          )}
        </Grid>
      </CardContent>
    </Card>
  );
};

export default DataQualityMetricsWidget;
```