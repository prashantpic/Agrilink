```typescript
import apiClient from '../../../../core/api/apiClient';
import { API_ENDPOINTS } from '../../../../core/api/apiEndpoints';
import { ApiResponse } from '../../../../types/api'; // Assuming common ApiResponse

// Specific types for Farmer Dashboard data structures
// These should ideally be in a farmerDashboardTypes.ts file or similar
interface LandSummaryData {
  totalArea: number;
  areaUnit: string;
  plotCount: number;
}

interface ActiveCrop {
  id: string;
  cropName: string;
  cultivatedArea: number;
  areaUnit: string;
  currentStage: string;
  status: string;
}

interface WeatherData {
  location: string;
  temperature: number;
  tempUnit: string;
  condition: string;
  precipitationChance?: number;
  iconCode?: string;
}

interface RecentQuery {
  id: string;
  querySummary: string;
  status: 'Open' | 'Answered' | 'Closed' | 'Pending';
  lastUpdate: string;
  unreadMessages: number;
}

interface FarmerAlert {
  id: string;
  type: 'info' | 'warning' | 'error' | 'success';
  message: string;
  timestamp: string;
}

interface HarvestKpi {
  label: string;
  value: number | string;
  unit?: string;
  cropName?: string;
  trend?: 'up' | 'down' | 'neutral';
}

interface KnowledgeBaseLink {
  id: string;
  title: string;
  url: string;
  relevance?: string;
}

export interface FarmerDashboardData {
  landSummary: LandSummaryData;
  activeCrops: ActiveCrop[];
  weatherSnippet: WeatherData;
  recentQueries: RecentQuery[];
  alerts: FarmerAlert[];
  harvestKpis: HarvestKpi[];
  knowledgeBaseLinks: KnowledgeBaseLink[];
}

interface DateRangeParams {
  startDate?: string;
  endDate?: string;
}

const getFarmerDashboardData = async (params?: DateRangeParams): Promise<FarmerDashboardData> => {
  try {
    const response = await apiClient.get<ApiResponse<FarmerDashboardData>>(
      API_ENDPOINTS.DASHBOARD.FARMER,
      { params }
    );
    // Assuming the API response structure nests the actual data under a 'data' property
    // and ApiResponse is generic { success: boolean, data: T, message?: string }
    if (response.data && response.data.success) {
      return response.data.data;
    }
    throw new Error(response.data.message || 'Failed to fetch farmer dashboard data');
  } catch (error) {
    // Axios error handling might place response data under error.response.data
    // You might want to standardize error throwing here
    console.error('Error fetching farmer dashboard data:', error);
    // @ts-ignore
    const errorMessage = error.response?.data?.message || error.message || 'An unknown error occurred';
    throw new Error(errorMessage);
  }
};

export const farmerDashboardApiService = {
  getFarmerDashboardData,
  // Potentially individual widget data fetch functions if backend supports it,
  // but current SDS implies a single fetch for the dashboard.
  // e.g., getLandSummary, getActiveCrops, etc. if needed for REQ-DASH-002...
};
```