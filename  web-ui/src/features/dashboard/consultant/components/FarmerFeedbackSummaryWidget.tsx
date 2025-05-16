```typescript
import React from 'react';
import { Card, CardContent, Typography, CircularProgress, Alert, Box, Rating, List, ListItem, ListItemText, Avatar } from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import ThumbUpAltIcon from '@mui/icons-material/ThumbUpAlt';
import ThumbDownAltIcon from '@mui/icons-material/ThumbDownAlt';
import KpiCard from '../../../dashboard/common/components/KpiCard';
import { KpiData } from '../../../dashboard/common/types/dashboardTypes';


interface Feedback {
    id: string;
    farmerName?: string; // Optional, for privacy
    rating?: number; // 1-5 stars
    comment?: string;
    timestamp: string;
}

interface FarmerFeedbackSummaryData {
  averageRating?: KpiData & { rawValue?: number};
  positiveFeedbackCount?: KpiData;
  negativeFeedbackCount?: KpiData;
  recentFeedbacks?: Feedback[]; // A few recent ones
}

interface FarmerFeedbackSummaryWidgetProps {
  data?: FarmerFeedbackSummaryData;
  isLoading: boolean;
  error?: Error | null;
}

const FarmerFeedbackSummaryWidget: React.FC<FarmerFeedbackSummaryWidgetProps> = ({ data, isLoading, error }) => {
  if (isLoading) {
    return <CircularProgress />;
  }

  if (error) {
    return <Alert severity="error">Error loading farmer feedback: {error.message}</Alert>;
  }

  if (!data || (!data.averageRating && (!data.recentFeedbacks || data.recentFeedbacks.length === 0))) {
    return (
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom component="div">
            Farmer Feedback
          </Typography>
          <Alert severity="info">No farmer feedback available at this time.</Alert>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom component="div">
          Farmer Feedback
        </Typography>
        {data.averageRating && (
            <Box sx={{display: 'flex', alignItems: 'center', justifyContent:'center', flexDirection:'column', mb: 2}}>
                <Typography variant="subtitle1">{data.averageRating.label}</Typography>
                <Rating value={data.averageRating.rawValue || 0} precision={0.1} readOnly emptyIcon={<StarIcon style={{ opacity: 0.55 }} fontSize="inherit" />} />
                <Typography variant="caption" color="text.secondary">{data.averageRating.value} ({data.averageRating.unit || 'stars'})</Typography>
            </Box>
        )}
        <Box sx={{display: 'flex', justifyContent: 'space-around', mb: 2}}>
            {data.positiveFeedbackCount && <KpiCard label={data.positiveFeedbackCount.label} value={data.positiveFeedbackCount.value} icon={<ThumbUpAltIcon color="success"/>} simple />}
            {data.negativeFeedbackCount && <KpiCard label={data.negativeFeedbackCount.label} value={data.negativeFeedbackCount.value} icon={<ThumbDownAltIcon color="error"/>} simple />}
        </Box>

        {data.recentFeedbacks && data.recentFeedbacks.length > 0 && (
          <>
            <Typography variant="subtitle2" gutterBottom sx={{mt:1}}>Recent Feedback</Typography>
            <List dense sx={{ maxHeight: 150, overflow: 'auto', bgcolor: 'background.paper', borderRadius: 1 }}>
              {data.recentFeedbacks.map((fb) => (
                <ListItem key={fb.id} alignItems="flex-start">
                  <ListItemText
                    primary={fb.farmerName ? `Feedback from ${fb.farmerName}` : 'Anonymous Feedback'}
                    secondary={
                      <>
                        {fb.rating && <Rating value={fb.rating} size="small" readOnly sx={{mr:1, verticalAlign: 'middle'}}/>}
                        {fb.comment}
                        <Typography component="span" variant="caption" sx={{display: 'block', color: 'text.disabled'}}>
                            {new Date(fb.timestamp).toLocaleDateString()}
                        </Typography>
                      </>
                    }
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

export default FarmerFeedbackSummaryWidget;
```