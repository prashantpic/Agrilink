```typescript
import React from 'react';
import { Card, CardContent, Typography, CircularProgress, Alert, Box } from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid'; // Assuming DataGrid is from MUI X

interface ActiveCrop {
  id: string;
  cropName: string;
  cultivatedArea: number;
  areaUnit: string;
  currentStage: string;
  status: string;
}

interface ActiveCropsWidgetProps {
  data?: ActiveCrop[];
  isLoading: boolean;
  error?: Error | null;
}

const columns: GridColDef[] = [
  { field: 'cropName', headerName: 'Crop Name', flex: 1 },
  { field: 'cultivatedArea', headerName: 'Area', flex: 0.5, type: 'number', valueGetter: (params) => `${params.row.cultivatedArea} ${params.row.areaUnit}` },
  { field: 'currentStage', headerName: 'Stage', flex: 1 },
  { field: 'status', headerName: 'Status', flex: 0.5 },
];

const ActiveCropsWidget: React.FC<ActiveCropsWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading active crops: {error.message}</Alert>;
  }

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Active Crops
          </Typography>
          <Alert severity="info">No active crops found.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Active Crops
        </Typography>
        <Box sx={{ height: 300, width: '100%' }}>
          <DataGrid
            rows={data}
            columns={columns}
            pageSizeOptions={[5]}
            disableRowSelectionOnClick
            autoHeight
          />
        </Box>
      </CardContent>
    </Card>
  );
};

export default ActiveCropsWidget;
```