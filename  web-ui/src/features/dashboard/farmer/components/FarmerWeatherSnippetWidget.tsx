```typescript
import React from 'react';
import { Card, CardContent, Typography, CircularProgress, Alert, Box, Avatar } from '@mui/material';
import WbSunnyIcon from '@mui/icons-material/WbSunny'; // Example icon
import CloudIcon from '@mui/icons-material/Cloud'; // Example icon
import GrainIcon from '@mui/icons-material/Grain'; // Example icon for precipitation

interface WeatherData {
  location: string;
  temperature: number;
  tempUnit: string;
  condition: string;
  precipitationChance?: number; // Percentage
  iconCode?: string; // For mapping to specific icons
}

interface FarmerWeatherSnippetWidgetProps {
  data?: WeatherData;
  isLoading: boolean;
  error?: Error | null;
}

const getWeatherIcon = (condition?: string, iconCode?: string) => {
  // This is a very basic example, a real implementation would map iconCode or condition text
  // to specific Material-UI icons or custom SVG icons.
  if (iconCode) {
    // Potentially map iconCode to an image URL or a specific component
  }
  if (condition?.toLowerCase().includes('sunny') || condition?.toLowerCase().includes('clear')) {
    return <WbSunnyIcon sx={{ fontSize: 40, color: 'orange' }} />;
  }
  if (condition?.toLowerCase().includes('cloud')) {
    return <CloudIcon sx={{ fontSize: 40, color: 'grey' }} />;
  }
  if (condition?.toLowerCase().includes('rain')) {
    return <GrainIcon sx={{ fontSize: 40, color: 'blue' }} />;
  }
  return <WbSunnyIcon sx={{ fontSize: 40 }} />; // Default
};


const FarmerWeatherSnippetWidget: React.FC<FarmerWeatherSnippetWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading weather data: {error.message}</Alert>;
  }

  if (!data) {
    return <Alert severity="info">No weather data available.</Alert>;
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Weather at {data.location}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Avatar sx={{ bgcolor: 'primary.light', width: 56, height: 56 }}>
            {getWeatherIcon(data.condition, data.iconCode)}
          </Avatar>
          <Box>
            <Typography variant="h4">
              {data.temperature}°{data.tempUnit}
            </Typography>
            <Typography variant="subtitle1" color="text.secondary">
              {data.condition}
            </Typography>
            {data.precipitationChance !== undefined && (
              <Typography variant="body2" color="text.secondary">
                Precipitation: {data.precipitationChance}%
              </Typography>
            )}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

export default FarmerWeatherSnippetWidget;
```