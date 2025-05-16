import { useState, useCallback } from 'react';
import { exportToCsv, exportToPdf } from '../services/exportService'; // Assuming these functions exist
// import { formatDataForCsv, formatDataForPdf } from '../utils/exportUtils'; // Assuming these utils exist

interface ExportState {
  isLoading: boolean;
  error: Error | null;
}

interface UseExportReturn {
  exportData: (data: any, format: 'csv' | 'pdf', fileName: string) => Promise<void>;
  isLoading: boolean;
  error: Error | null;
}

const useExport = (): UseExportReturn => {
  const [exportState, setExportState] = useState<ExportState>({
    isLoading: false,
    error: null,
  });

  const exportData = useCallback(async (data: any, format: 'csv' | 'pdf', fileName: string) => {
    setExportState({ isLoading: true, error: null });
    try {
      if (format === 'csv') {
        // const csvData = formatDataForCsv(data); // Data might need specific formatting
        await exportToCsv(data, fileName); // Assuming exportToCsv handles data conversion or accepts raw data
      } else if (format === 'pdf') {
        // const pdfData = formatDataForPdf(data); // Data might need specific formatting
        await exportToPdf(data, fileName); // Assuming exportToPdf handles data to document definition conversion
      } else {
        throw new Error('Unsupported export format');
      }
      setExportState({ isLoading: false, error: null });
    } catch (err) {
      console.error('Export error:', err);
      setExportState({ isLoading: false, error: err instanceof Error ? err : new Error('Export failed') });
    }
  }, []);

  return {
    exportData,
    isLoading: exportState.isLoading,
    error: exportState.error,
  };
};

export default useExport;