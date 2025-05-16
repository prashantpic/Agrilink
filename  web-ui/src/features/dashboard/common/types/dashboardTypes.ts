export interface KpiData {
  label: string;
  value: number | string;
  unit?: string;
  trend?: 'up' | 'down' | 'neutral';
  icon?: React.ElementType;
  color?: string;
  previousValue?: number | string; // For more detailed trend calculation or display
  description?: string; // Additional context
}

export interface ChartDataset {
  label: string;
  data: number[];
  backgroundColor?: string | string[];
  borderColor?: string | string[];
  fill?: boolean;
  tension?: number;
  // Allow any other chart.js dataset properties
  [key: string]: any;
}

export interface ChartData {
  labels: string[];
  datasets: ChartDataset[];
  chartType: 'bar' | 'line' | 'pie' | 'doughnut' | 'radar' | 'polarArea';
  options?: any; // Chart.js options
}

export interface AlertMessage { // Renamed from AlertNotification to avoid conflict with component name
  id: string;
  type: 'info' | 'warning' | 'error' | 'success';
  message: string;
  timestamp: string;
  source?: string;
  read?: boolean;
  details?: string; // For more detailed error messages
}

export interface MapLayer {
  id: string;
  type: 'marker' | 'polygon' | 'polyline' | 'geojson';
  data: any; // e.g., [lat, lng] for marker, array of [lat,lng] for polyline, GeoJSON object
  tooltip?: string;
  popup?: string;
  options?: any; // Leaflet layer options
}

export interface MapData {
  center: [number, number]; // [latitude, longitude]
  zoom: number;
  layers?: MapLayer[];
  tileLayerUrl?: string;
  tileLayerAttribution?: string;
}

// For DataGrid, Material-UI's GridColDef can be used directly for columns.
// Rows will be an array of objects, where keys match 'field' in GridColDef.
// Example:
// interface MyDataRow { id: number; name: string; age: number; }
// const rows: MyDataRow[] = [...];
// const columns: GridColDef[] = [{ field: 'name', headerName: 'Name', width: 150 }];

// General Dashboard Data Structure (example for a specific dashboard role)
// This will be more specific within each dashboard's types, e.g. FarmerDashboardData
export interface BaseDashboardData {
  // Common fields if any, or role-specific data structures
  [key: string]: any; // Placeholder for more specific types
}

export interface DateRange {
  start: Date | null;
  end: Date | null;
}