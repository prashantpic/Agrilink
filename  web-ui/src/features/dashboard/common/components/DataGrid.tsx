import React from 'react';
import { DataGrid as MuiDataGrid, GridColDef, GridRowsProp, DataGridProps } from '@mui/x-data-grid';
import { Paper, Typography, Box } from '@mui/material';

interface CustomDataGridProps extends Omit<DataGridProps, 'rows' | 'columns'> {
  rows: GridRowsProp;
  columns: GridColDef[];
  title?: string;
  height?: string | number;
  // Add any other custom props you need
}

const DataGrid: React.FC<CustomDataGridProps> = ({
  rows,
  columns,
  title,
  height = 400,
  ...rest
}) => {
  return (
    <Paper sx={{ height, width: '100%', p: title ? 2 : 0 }}>
      {title && (
        <Typography variant="h6" gutterBottom component="div">
          {title}
        </Typography>
      )}
      <Box sx={{ height: title ? `calc(${height}px - 48px)` : '100%', width: '100%' }}>
        <MuiDataGrid
          rows={rows}
          columns={columns}
          pageSizeOptions={[5, 10, 25]}
          initialState={{
            pagination: {
              paginationModel: { pageSize: 10, page: 0 },
            },
          }}
          // Enable features as needed
          // filterable
          // sortable
          // resizable
          density="compact"
          sx={{ border: 0 }} // Remove default border if inside Paper
          {...rest}
        />
      </Box>
    </Paper>
  );
};

export default DataGrid;