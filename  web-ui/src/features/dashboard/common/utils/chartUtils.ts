import { ChartData, ChartDataset } from '../types/dashboardTypes';

// Default options for different chart types
export const getDefaultChartOptions = (chartType: ChartData['chartType']) => {
  const commonOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
      },
      tooltip: {
        mode: 'index' as const,
        intersect: false,
      },
    },
    scales: {},
  };

  switch (chartType) {
    case 'bar':
    case 'line':
      (commonOptions.scales as any) = { // Type assertion for scales
        x: {
          beginAtZero: true,
          grid: {
            display: false,
          },
        },
        y: {
          beginAtZero: true,
          grid: {
            color: 'rgba(200, 200, 200, 0.2)',
          },
        },
      };
      break;
    case 'pie':
    case 'doughnut':
    case 'polarArea':
      // Specific options for these types, e.g., no scales
      delete commonOptions.scales;
      break;
    case 'radar':
      (commonOptions.scales as any) = { // Type assertion for scales
        r: {
          angleLines: {
            display: true,
          },
          suggestedMin: 0,
          // suggestedMax: can be set based on data
        },
      };
      break;
    default:
      break;
  }
  return commonOptions;
};

/**
 * Transforms raw API data into a format suitable for Chart.js.
 * This is a placeholder and needs to be adapted based on actual API response structures.
 * @param rawData - The data fetched from an API.
 * @param labelField - The field in rawData items to use for chart labels.
 * @param valueField - The field in rawData items to use for chart data values.
 * @param datasetLabel - The label for the dataset.
 * @returns ChartDataset object.
 */
export const transformDataForChart = (
  rawData: any[],
  labelField: string,
  valueField: string,
  datasetLabel: string
): Pick<ChartData, 'labels' | 'datasets'> => {
  if (!rawData || rawData.length === 0) {
    return { labels: [], datasets: [] };
  }

  const labels = rawData.map(item => item[labelField]);
  const dataValues = rawData.map(item => item[valueField]);

  const dataset: ChartDataset = {
    label: datasetLabel,
    data: dataValues,
    // Default styling, can be overridden
    backgroundColor: 'rgba(75, 192, 192, 0.6)',
    borderColor: 'rgba(75, 192, 192, 1)',
    borderWidth: 1,
  };

  return {
    labels,
    datasets: [dataset],
  };
};

// Example: Generate an array of distinct colors for chart datasets
export const generateChartColors = (count: number): { backgroundColors: string[], borderColors: string[] } => {
  const baseColors = [
    'rgba(255, 99, 132, 0.6)',  // Red
    'rgba(54, 162, 235, 0.6)', // Blue
    'rgba(255, 206, 86, 0.6)', // Yellow
    'rgba(75, 192, 192, 0.6)', // Green
    'rgba(153, 102, 255, 0.6)',// Purple
    'rgba(255, 159, 64, 0.6)', // Orange
  ];
  const backgroundColors: string[] = [];
  const borderColors: string[] = [];

  for (let i = 0; i < count; i++) {
    const color = baseColors[i % baseColors.length];
    backgroundColors.push(color);
    borderColors.push(color.replace('0.6', '1')); // Make border opaque
  }
  return { backgroundColors, borderColors };
};