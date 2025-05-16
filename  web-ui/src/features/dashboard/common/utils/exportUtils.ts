/**
 * Formats an array of objects into a structure suitable for CSV generation by papaparse.
 * Papaparse can often handle an array of objects directly, but this function
 * can be used for more complex transformations if needed (e.g., flattening nested objects,
 * renaming headers, formatting specific values).
 *
 * @param data - An array of objects.
 * @param columnOrder - Optional array of strings specifying column order and inclusion.
 * @param headerMap - Optional mapping from data keys to CSV header names.
 * @returns An array of objects formatted for CSV, or the original data if no transformation is needed.
 */
export const formatDataForCsv = (
    data: any[],
    columnOrder?: string[],
    headerMap?: { [key: string]: string }
  ): any[] => {
    if (!data || data.length === 0) {
      return [];
    }
  
    if (!columnOrder && !headerMap) {
      // Papaparse can handle array of objects directly
      return data;
    }
  
    return data.map(row => {
      const newRow: { [key: string]: any } = {};
      const keysToProcess = columnOrder || Object.keys(row);
  
      keysToProcess.forEach(key => {
        if (row.hasOwnProperty(key)) {
          const headerName = headerMap && headerMap[key] ? headerMap[key] : key;
          // Basic value formatting (e.g., for dates, booleans) can be added here
          let value = row[key];
          if (value instanceof Date) {
            value = value.toISOString().split('T')[0]; // Format date as YYYY-MM-DD
          } else if (typeof value === 'boolean') {
            value = value ? 'Yes' : 'No';
          }
          newRow[headerName] = value;
        }
      });
      return newRow;
    });
  };
  
  /**
   * Formats data into a document definition structure suitable for pdfmake.
   * This function needs to be highly customized based on the specific data
   * and desired PDF layout.
   *
   * @param data - The data to be formatted for the PDF. This could be a complex object.
   * @param title - The title for the PDF document.
   * @returns A pdfmake document definition object.
   */
  export const formatDataForPdf = (data: any, title: string): any => {
    // This is a very basic example. Real-world PDF generation will be more complex.
    // The structure of 'data' will dictate how you build tables, lists, paragraphs, etc.
  
    const content: any[] = [
      { text: title, style: 'header' },
      { text: `Report Generated: ${new Date().toLocaleString()}`, style: 'subheader', margin: [0, 0, 0, 20] as [number,number,number,number] },
    ];
  
    // Example: If data is an array of objects, create a simple table
    if (Array.isArray(data) && data.length > 0 && typeof data[0] === 'object') {
      const tableHeader = Object.keys(data[0]).map(key => ({ text: key, style: 'tableHeader' }));
      const tableBody = data.map(row => Object.values(row).map(value => String(value)));
  
      content.push({
        table: {
          headerRows: 1,
          widths: Array(tableHeader.length).fill('*'), // Distribute column widths equally
          body: [tableHeader, ...tableBody],
        },
        layout: 'lightHorizontalLines', // Optional table styling
      });
    } else if (typeof data === 'object' && data !== null) {
      // Example: If data is an object, list its key-value pairs
      Object.entries(data).forEach(([key, value]) => {
        content.push({ text: `${key}: ${JSON.stringify(value)}`, margin: [0, 5, 0, 5] as [number,number,number,number] });
      });
    } else {
      content.push({ text: String(data) });
    }
  
    return {
      content: content,
      styles: {
        header: { fontSize: 22, bold: true, alignment: 'center', margin: [0,0,0,20] as [number,number,number,number] },
        subheader: { fontSize: 10, italics: true, alignment: 'right' },
        tableHeader: { bold: true, fontSize: 12, color: 'black' },
      },
      defaultStyle: {
        // font: 'Roboto' // Ensure fonts are configured in exportService.ts
      }
    };
  };