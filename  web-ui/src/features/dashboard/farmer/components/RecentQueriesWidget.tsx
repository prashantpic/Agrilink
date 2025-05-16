```typescript
import React from 'react';
import { Card, CardContent, Typography, CircularProgress, Alert, Box, Chip } from '@mui/material';
import { DataGrid, GridColDef } from '@mui/x-data-grid';

interface RecentQuery {
  id: string;
  querySummary: string;
  status: 'Open' | 'Answered' | 'Closed' | 'Pending';
  lastUpdate: string; // ISO Date string
  unreadMessages: number;
}

interface RecentQueriesWidgetProps {
  data?: RecentQuery[];
  isLoading: boolean;
  error?: Error | null;
}

const getStatusChipColor = (status: RecentQuery['status']): "default" | "primary" | "secondary" | "error" | "info" | "success" | "warning" => {
  switch (status) {
    case 'Open': return 'info';
    case 'Answered': return 'success';
    case 'Closed': return 'default';
    case 'Pending': return 'warning';
    default: return 'default';
  }
}

const columns: GridColDef<RecentQuery>[] = [
  { field: 'querySummary', headerName: 'Query', flex: 2 },
  {
    field: 'status',
    headerName: 'Status',
    flex: 1,
    renderCell: (params) => (
      <Chip label={params.value} color={getStatusChipColor(params.value)} size="small" />
    ),
  },
  {
    field: 'lastUpdate',
    headerName: 'Last Update',
    flex: 1,
    type: 'dateTime',
    valueGetter: (params) => new Date(params.value),
  },
  {
    field: 'unreadMessages',
    headerName: 'Unread',
    flex: 0.5,
    type: 'number',
    renderCell: (params) => (
        params.value > 0 ? <Chip label={params.value} color="error" size="small" /> : null
    )
  },
];

const RecentQueriesWidget: React.FC<RecentQueriesWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading recent queries: {error.message}</Alert>;
  }

  if (!data || data.length === 0) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Recent Queries
          </Typography>
          <Alert severity="info">No recent queries found.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Recent Queries
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

export default RecentQueriesWidget;
```