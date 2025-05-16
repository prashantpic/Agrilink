import React from 'react';
import { Alert, AlertTitle, Typography, Box } from '@mui/material';
import { AlertMessage } from '../types/dashboardTypes'; // Renamed to AlertMessage to avoid name collision

interface AlertNotificationProps {
  alert: AlertMessage;
  onClose?: () => void; // Optional: if the alert can be dismissed
}

const AlertNotification: React.FC<AlertNotificationProps> = ({ alert, onClose }) => {
  return (
    <Alert
      severity={alert.type}
      onClose={onClose}
      sx={{ width: '100%', mb: 2 }}
    >
      <AlertTitle>
        {alert.type.charAt(0).toUpperCase() + alert.type.slice(1)}
        {alert.source && ` - ${alert.source}`}
      </AlertTitle>
      <Typography variant="body2">{alert.message}</Typography>
      {alert.details && (
        <Typography variant="caption" display="block" sx={{ mt: 1, wordBreak: 'break-all' }}>
          Details: {alert.details}
        </Typography>
      )}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt:1 }}>
        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
          ID: {alert.id}
        </Typography>
        <Typography variant="caption" sx={{ color: 'text.secondary' }}>
          {new Date(alert.timestamp).toLocaleString()}
        </Typography>
      </Box>
    </Alert>
  );
};

export default AlertNotification;