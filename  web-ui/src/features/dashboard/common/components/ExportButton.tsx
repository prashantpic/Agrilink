import React, { useState } from 'react';
import { Button as MuiButton, Menu, MenuItem, CircularProgress } from '@mui/material';
import { Download as DownloadIcon } from '@mui/icons-material';
import { useExport } from '../hooks/useExport'; // Assuming useExport hook is created

interface ExportButtonProps {
  data: any; // Data to be exported, structure depends on what widgets provide
  fileNamePrefix?: string;
  disabled?: boolean;
}

const ExportButton: React.FC<ExportButtonProps> = ({
  data,
  fileNamePrefix = 'dashboard-export',
  disabled = false,
}) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const { exportData, isLoading, error } = useExport();

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleExport = async (format: 'csv' | 'pdf') => {
    handleClose();
    if (!data) {
      console.error('No data available for export.');
      // Potentially show a notification to the user
      return;
    }
    await exportData(data, format, `${fileNamePrefix}-${new Date().toISOString().split('T')[0]}`);
    // Error handling is within useExport, but can show notifications here too
    if (error) {
      console.error('Export failed:', error);
      // Show error notification
    }
  };

  return (
    <>
      <MuiButton
        variant="contained"
        color="secondary"
        startIcon={isLoading ? <CircularProgress size={20} color="inherit" /> : <DownloadIcon />}
        onClick={handleClick}
        disabled={disabled || isLoading || !data}
        aria-controls="export-menu"
        aria-haspopup="true"
      >
        Export
      </MuiButton>
      <Menu
        id="export-menu"
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleClose}
      >
        <MenuItem onClick={() => handleExport('csv')} disabled={isLoading}>
          Export as CSV
        </MenuItem>
        <MenuItem onClick={() => handleExport('pdf')} disabled={isLoading}>
          Export as PDF
        </MenuItem>
      </Menu>
    </>
  );
};

export default ExportButton;