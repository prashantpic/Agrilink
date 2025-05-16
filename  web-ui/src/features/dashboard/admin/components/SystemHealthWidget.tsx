```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert, List, ListItem, ListItemText, ListItemIcon, Chip } from '@mui/material';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import ReportProblemIcon from '@mui/icons-material/ReportProblem';
import DnsIcon from '@mui/icons-material/Dns'; // For services
import MemoryIcon from '@mui/icons-material/Memory'; // For resources
import StorageIcon from '@mui/icons-material/Storage'; // For DB
import { KpiData } from '../../../dashboard/common/types/dashboardTypes';
import KpiCard from '../../../dashboard/common/components/KpiCard';

interface HealthIndicator {
  id: string;
  name: string;
  status: 'Healthy' | 'Warning' | 'Critical' | 'Unknown';
  details?: string;
}

interface SystemHealthData {
  overallStatus: KpiData & { statusValue: HealthIndicator['status'] }; // e.g. { label: "Overall System", value: "Healthy", statusValue: "Healthy"}
  serviceStatuses?: HealthIndicator[];
  resourceUtilization?: KpiData[]; // e.g., CPU, Memory with values
  recentCriticalLogsCount?: KpiData;
  dbPerformance?: KpiData; // e.g., { label: "DB Query Time", value: "50ms" }
}

interface SystemHealthWidgetProps {
  data?: SystemHealthData;
  isLoading: boolean;
  error?: Error | null;
}

const getStatusIcon = (status: HealthIndicator['status']) => {
  switch (status) {
    case 'Healthy': return <CheckCircleOutlineIcon color="success" />;
    case 'Warning': return <ReportProblemIcon color="warning" />;
    case 'Critical': return <ErrorOutlineIcon color="error" />;
    default: return <DnsIcon color="disabled"/>;
  }
};

const getStatusChipColor = (status: HealthIndicator['status']): "success" | "warning" | "error" | "default" => {
   switch (status) {
    case 'Healthy': return 'success';
    case 'Warning': return 'warning';
    case 'Critical': return 'error';
    default: return 'default';
  }
}

const SystemHealthWidget: React.FC<SystemHealthWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading system health: {error.message}</Alert>;
  }

  if (!data) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            System Health
          </Typography>
          <Alert severity="info">No system health data available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          System Health
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={12}>
             <KpiCard
                label={data.overallStatus.label}
                value={data.overallStatus.value}
                // @ts-ignore // Allow custom styling based on statusValue
                valueColor={getStatusChipColor(data.overallStatus.statusValue)}
             />
          </Grid>

          {data.serviceStatuses && data.serviceStatuses.length > 0 && (
            <Grid item xs={12} md={6}>
              <Typography variant="subtitle1" gutterBottom>Service Statuses</Typography>
              <List dense sx={{ maxHeight: 200, overflow: 'auto', border: '1px solid #eee', borderRadius:1}}>
                {data.serviceStatuses.map(service => (
                  <ListItem key={service.id} secondaryAction={
                    <Chip label={service.status} color={getStatusChipColor(service.status)} size="small"/>
                  }>
                    <ListItemIcon sx={{minWidth: 30}}>{getStatusIcon(service.status)}</ListItemIcon>
                    <ListItemText primary={service.name} secondary={service.details} />
                  </ListItem>
                ))}
              </List>
            </Grid>
          )}

          <Grid item xs={12} md={6}>
             {data.resourceUtilization && data.resourceUtilization.length > 0 && (
                <>
                    <Typography variant="subtitle1" gutterBottom>Resource Utilization</Typography>
                    <Grid container spacing={1}>
                    {data.resourceUtilization.map(kpi => (
                        <Grid item xs={6} key={kpi.label}>
                            <KpiCard label={kpi.label} value={kpi.value} unit={kpi.unit} simple />
                        </Grid>
                    ))}
                    </Grid>
                </>
             )}
             {data.recentCriticalLogsCount && (
                <Box mt={2}>
                    <KpiCard label={data.recentCriticalLogsCount.label} value={data.recentCriticalLogsCount.value} simple />
                </Box>
             )}
             {data.dbPerformance && (
                <Box mt={1}>
                    <KpiCard label={data.dbPerformance.label} value={data.dbPerformance.value} simple />
                </Box>
             )}
          </Grid>
        </Grid>
      </CardContent>
    </Card>
  );
};

export default SystemHealthWidget;
```