```typescript
import React from 'react';
import { Card, CardContent, Typography, CircularProgress, Alert, Box } from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid';

interface MasterDataLogEntry {
  id: string;
  itemType: string; // e.g., "CropType", "SoilType", "UserRole"
  itemName: string; // e.g., "Wheat", "Loamy", "Farm Plot Admin"
  action: 'Created' | 'Updated' | 'Deleted';
  adminUser: string; // Username or ID of admin
  timestamp: string; // ISO Date string
  details?: string; // e.g. "Name changed from 'X' to 'Y'"
}

interface MasterDataLogsWidgetProps {
  data?: MasterDataLogEntry[];
  isLoading: boolean;
  error?: Error | null;
}

const columns: GridColDef<MasterDataLogEntry>[] = [
  {
    field: 'timestamp',
    headerName: 'Timestamp',
    flex: 1,
    type: 'dateTime',
    valueGetter: (params) => new Date(params.value),
  },
  { field: 'itemType', headerName: 'Item Type', flex: 1 },
  { field: 'itemName', headerName: 'Item Name', flex: 1 },
  { field: 'action', headerName: 'Action', flex: 0.5 },
  { field: 'adminUser', headerName: 'Admin User', flex: 1 },
  { field: 'details', headerName: 'Details', flex: 1.5, sortable: false },
];

const MasterDataLogsWidget: React.FC<MasterDataLogsWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading master data logs: {error.message}</Alert>;
  }

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Recent Master Data Updates
          </Typography>
          <Alert severity="info">No recent master data logs found.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Recent Master Data Updates
        </Typography>
        <Box sx={{ height: 350, width: '100%' }}>
          <DataGrid
            rows={data}
            columns={columns}
            pageSizeOptions={[5, 10]}
            disableRowSelectionOnClick
            autoHeight
          />
        </Box>
      </CardContent>
    </Card>
  );
};

export default MasterDataLogsWidget;
```