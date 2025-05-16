```typescript
import apiClient from '../../../../core/api/apiClient';
import { API_ENDPOINTS } from '../../../../core/api/apiEndpoints';
import { ApiResponse } from '../../../../types/api';
import { ChartData, KpiData } from '../../../dashboard/common/types/dashboardTypes';

// Define specific types for Admin Dashboard data structures
// These should ideally be in an adminDashboardTypes.ts file
interface SystemStat {
  label: string;
  value: number | string;
  unit?: string;
}

interface UserRegistrationTrendData {
  trendChartData: ChartData;
  pendingApprovals: KpiData;
}

interface ConsultantPerformance {
  id: string;
  name: string;
  resolvedQueries: number;
  avgResolutionTime: string;
}

interface QuerySystemOverviewData {
  totalQueries: KpiData;
  openClosedRatioChart?: ChartData;
  avgResolutionTime: KpiData;
  slaAdherence: KpiData;
  consultantLeaderboard?: ConsultantPerformance[];
}

interface ApiKeyUsage {
  id: string;
  apiKeyAlias: string;
  requestCount: number;
  errorRate: number;
}

interface ApiUsageStatsData {
  totalRequests: KpiData;
  errorRateOverall: KpiData;
  requestsOverTimeChart?: ChartData;
  apiKeyUsageSummary?: ApiKeyUsage[];
}
interface HealthIndicator {
  id: string;
  name: string;
  status: 'Healthy' | 'Warning' | 'Critical' | 'Unknown';
  details?: string;
}
interface SystemHealthData {
  overallStatus: KpiData & { statusValue: HealthIndicator['status'] };
  serviceStatuses?: HealthIndicator[];
  resourceUtilization?: KpiData[];
  recentCriticalLogsCount?: KpiData;
  dbPerformance?: KpiData;
}

interface DataQualityMetric {
  id: string;
  label: string;
  value: number;
  description?: string;
}
interface DataQualityMetricsData {
  metrics: DataQualityMetric[];
  overallQualityScore?: KpiData;
  completenessChart?: ChartData;
}

interface MasterDataLogEntry {
  id: string;
  itemType: string;
  itemName: string;
  action: 'Created' | 'Updated' | 'Deleted';
  adminUser: string;
  timestamp: string;
  details?: string;
}

export interface AdminDashboardData {
  systemStats: SystemStat[];
  userRegistrationTrend: UserRegistrationTrendData;
  querySystemOverview: QuerySystemOverviewData;
  apiUsageStats: ApiUsageStatsData;
  systemHealth: SystemHealthData;
  dataQualityMetrics: DataQualityMetricsData;
  masterDataLogs: MasterDataLogEntry[];
}

interface DateRangeParams {
  startDate?: string;
  endDate?: string;
}

const getAdminDashboardData = async (params?: DateRangeParams): Promise<AdminDashboardData> => {
  try {
    const response = await apiClient.get<ApiResponse<AdminDashboardData>>(
      API_ENDPOINTS.DASHBOARD.ADMIN,
      { params }
    );
    if (response.data && response.data.success) {
      return response.data.data;
    }
    throw new Error(response.data.message || 'Failed to fetch admin dashboard data');
  } catch (error) {
    console.error('Error fetching admin dashboard data:', error);
    // @ts-ignore
    const errorMessage = error.response?.data?.message || error.message || 'An unknown error occurred';
    throw new Error(errorMessage);
  }
};

export const adminDashboardApiService = {
  getAdminDashboardData,
};
```