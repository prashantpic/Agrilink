import React, { useEffect, useRef } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, PointElement, LineElement, RadialLinearScale, Filler } from 'chart.js';
import { Chart } from 'react-chartjs-2';
import { Box, Paper, Typography } from '@mui/material';
import { ChartData as ChartWidgetData } from '../types/dashboardTypes'; // Renamed to avoid name collision
import { getDefaultChartOptions } from '../utils/chartUtils'; // Assuming this utility exists

// Register Chart.js components
ChartJS.register(
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  RadialLinearScale,
  Filler
);

interface ChartWidgetProps {
  chartData: ChartWidgetData;
  title?: string;
  height?: string | number;
}

const ChartWidget: React.FC<ChartWidgetProps> = ({ chartData, title, height = 300 }) => {
  const chartRef = useRef<ChartJS>(null);

  // Example of how to handle dynamic options or updates if needed
  useEffect(() => {
    // Access chart instance via ref if necessary, e.g., chartRef.current.update()
  }, [chartData]);

  const options = {
    ...getDefaultChartOptions(chartData.chartType), // Get some sensible defaults
    ...(chartData.options || {}), // Merge with user-provided options
    responsive: true,
    maintainAspectRatio: false,
  };

  return (
    <Paper sx={{ p: 2, height: `calc(${height}px + 48px)`, display: 'flex', flexDirection: 'column' }}>
      {title && (
        <Typography variant="h6" gutterBottom component="div">
          {title}
        </Typography>
      )}
      <Box sx={{ position: 'relative', height: `${height}px`, width: '100%' }}>
        <Chart
            ref={chartRef}
            type={chartData.chartType}
            data={{ labels: chartData.labels, datasets: chartData.datasets }}
            options={options}
        />
      </Box>
    </Paper>
  );
};

export default ChartWidget;