import React from 'react';
import { Card, CardContent, Typography, Box, Icon, Tooltip } from '@mui/material';
import { ArrowUpward, ArrowDownward, Remove } from '@mui/icons-material';
import { KpiData } from '../types/dashboardTypes';

interface KpiCardProps {
  kpi: KpiData;
  onClick?: () => void;
}

const KpiCard: React.FC<KpiCardProps> = ({ kpi, onClick }) => {
  const TrendIcon = kpi.trend === 'up' ? ArrowUpward : kpi.trend === 'down' ? ArrowDownward : Remove;
  const trendColor = kpi.trend === 'up' ? 'success.main' : kpi.trend === 'down' ? 'error.main' : 'text.secondary';

  return (
    <Card sx={{ minWidth: 200, cursor: onClick ? 'pointer' : 'default' }} onClick={onClick}>
      <CardContent>
        {kpi.icon && (
          <Icon component={kpi.icon} sx={{ fontSize: 30, color: kpi.color || 'primary.main', float: 'right', opacity: 0.8 }} />
        )}
        <Typography sx={{ fontSize: 14, color: 'text.secondary', gutterBottom }}>
          {kpi.label}
        </Typography>
        <Typography variant="h4" component="div" sx={{ fontWeight: 'bold' }}>
          {kpi.value}
          {kpi.unit && <Typography variant="h6" component="span" sx={{ ml: 0.5, fontWeight: 'normal' }}>{kpi.unit}</Typography>}
        </Typography>
        {kpi.trend && (
          <Box sx={{ display: 'flex', alignItems: 'center', mt: 1, color: trendColor }}>
            <TrendIcon sx={{ fontSize: 20 }} />
            <Typography variant="body2" sx={{ ml: 0.5 }}>
              {/* Optionally show percentage change or previous value here */}
              {kpi.trend !== 'neutral' ? `${kpi.trend === 'up' ? 'Increase' : 'Decrease'}` : 'Stable'}
              {kpi.previousValue && ` (from ${kpi.previousValue})`}
            </Typography>
          </Box>
        )}
        {kpi.description && (
          <Tooltip title={kpi.description}>
            <Typography variant="caption" display="block" sx={{ mt: 1, color: 'text.secondary', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {kpi.description}
            </Typography>
          </Tooltip>
        )}
      </CardContent>
    </Card>
  );
};

export default KpiCard;