```typescript
import apiClient from '../../../../core/api/apiClient';
import { API_ENDPOINTS } from '../../../../core/api/apiEndpoints';
import { ApiResponse } from '../../../../types/api';
import { KpiData } from '../../../dashboard/common/types/dashboardTypes';

// Define specific types for Consultant Dashboard data structures
interface QueryLoadStat {
  label: string;
  value: number;
}

interface PerformanceMetric {
  label: string;
  value: string;
  unit?: string;
  trend?: 'up' | 'down' | 'neutral';
}

interface Feedback {
    id: string;
    farmerName?: string;
    rating?: number;
    comment?: string;
    timestamp: string;
}
interface FarmerFeedbackSummaryData {
  averageRating?: KpiData & { rawValue?: number};
  positiveFeedbackCount?: KpiData;
  negativeFeedbackCount?: KpiData;
  recentFeedbacks?: Feedback[];
}

interface RecentAnswer {
  id: string;
  queryId: string;
  querySummary: string;
  actionTaken: string;
  actionTimestamp: string;
  currentQueryStatus: 'Open' | 'Answered' | 'Closed' | 'Pending Farmer';
}

interface KbArticleContribution {
    id: string;
    title: string;
    status: 'Draft' | 'Pending Approval' | 'Approved' | 'Rejected';
    lastUpdated: string;
}
interface KbContributionsData {
  totalContributions: KpiData;
  approvedArticles: KpiData;
  pendingApproval: KpiData;
  recentContributions?: KbArticleContribution[];
}

export interface ConsultantDashboardData {
  queryLoad: QueryLoadStat[];
  performanceMetrics: PerformanceMetric[];
  feedbackSummary: FarmerFeedbackSummaryData;
  recentAnswers: RecentAnswer[];
  kbContributions: KbContributionsData;
}

interface DateRangeParams {
  startDate?: string;
  endDate?: string;
}

const getConsultantDashboardData = async (params?: DateRangeParams): Promise<ConsultantDashboardData> => {
  try {
    const response = await apiClient.get<ApiResponse<ConsultantDashboardData>>(
      API_ENDPOINTS.DASHBOARD.CONSULTANT, // Ensure CONSULTANT endpoint is defined
      { params }
    );
    if (response.data && response.data.success) {
      return response.data.data;
    }
    throw new Error(response.data.message || 'Failed to fetch Consultant dashboard data');
  } catch (error) {
    console.error('Error fetching Consultant dashboard data:', error);
    // @ts-ignore
    const errorMessage = error.response?.data?.message || error.message || 'An unknown error occurred';
    throw new Error(errorMessage);
  }
};

export const consultantDashboardApiService = {
  getConsultantDashboardData,
};
```