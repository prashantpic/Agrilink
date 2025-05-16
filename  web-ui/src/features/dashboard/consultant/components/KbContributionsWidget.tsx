```typescript
import React from 'react';
import { Card, CardContent, Typography, Grid, CircularProgress, Alert, List, ListItem, ListItemText, ListItemIcon, Chip } from '@mui/material';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { KpiData } from '../../../dashboard/common/types/dashboardTypes';
import ArticleIcon from '@mui/icons-material/Article';
import PendingActionsIcon from '@mui/icons-material/PendingActions';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import UnpublishedIcon from '@mui/icons-material/Unpublished';


interface KbArticleContribution {
    id: string;
    title: string;
    status: 'Draft' | 'Pending Approval' | 'Approved' | 'Rejected';
    lastUpdated: string; // ISO Date string
}

interface KbContributionsData {
  totalContributions: KpiData;
  approvedArticles: KpiData;
  pendingApproval: KpiData;
  recentContributions?: KbArticleContribution[]; // List a few recent ones
}

interface KbContributionsWidgetProps {
  data?: KbContributionsData;
  isLoading: boolean;
  error?: Error | null;
}

const getArticleStatusIcon = (status: KbArticleContribution['status']) => {
    switch(status) {
        case 'Approved': return <CheckCircleIcon color="success" />;
        case 'Pending Approval': return <PendingActionsIcon color="warning" />;
        case 'Rejected': return <UnpublishedIcon color="error" />;
        case 'Draft':
        default: return <ArticleIcon color="action" />;
    }
}

const KbContributionsWidget: React.FC<KbContributionsWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading KB contributions: {error.message}</Alert>;
  }

  if (!data) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Knowledge Base Contributions
          </Typography>
          <Alert severity="info">No contribution data available.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Knowledge Base Contributions
        </Typography>
        <Grid container spacing={2} sx={{mb: 2}}>
          <Grid item xs={12} sm={4}>
            <KpiCard label={data.totalContributions.label} value={data.totalContributions.value} />
          </Grid>
          <Grid item xs={12} sm={4}>
            <KpiCard label={data.approvedArticles.label} value={data.approvedArticles.value} />
          </Grid>
          <Grid item xs={12} sm={4}>
            <KpiCard label={data.pendingApproval.label} value={data.pendingApproval.value} />
          </Grid>
        </Grid>

        {data.recentContributions && data.recentContributions.length > 0 && (
          <>
            <Typography variant="subtitle2" gutterBottom>Recent Contributions</Typography>
            <List dense sx={{ maxHeight: 150, overflow: 'auto', bgcolor: 'background.paper', borderRadius:1 }}>
              {data.recentContributions.map((contrib) => (
                <ListItem
                  key={contrib.id}
                  secondaryAction={
                    <Chip label={contrib.status} size="small" />
                  }
                >
                  <ListItemIcon sx={{minWidth: 35}}>
                    {getArticleStatusIcon(contrib.status)}
                  </ListItemIcon>
                  <ListItemText
                    primary={contrib.title}
                    secondary={`Last updated: ${new Date(contrib.lastUpdated).toLocaleDateString()}`}
                  />
                </ListItem>
              ))}
            </List>
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default KbContributionsWidget;
```