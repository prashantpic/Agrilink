import Papa from 'papaparse';
import pdfMake from 'pdfmake/build/pdfmake';
import pdfFonts from 'pdfmake/build/vfs_fonts'; // Virtual file system for fonts

// Initialize pdfMake with fonts
if (pdfMake.vfs === undefined || Object.keys(pdfMake.vfs).length === 0) {
    pdfMake.vfs = pdfFonts.pdfMake.vfs;
}
// It's common to set default fonts for pdfMake if not using specific ones.
// pdfMake.fonts = {
//   Roboto: {
//     normal: 'Roboto-Regular.ttf',
//     bold: 'Roboto-Medium.ttf',
//     italics: 'Roboto-Italic.ttf',
//     bolditalics: 'Roboto-MediumItalic.ttf'
//   }
// };

/**
 * Exports data to a CSV file and triggers a download.
 * @param data - Array of objects to export.
 * @param filename - The name of the CSV file (without .csv extension).
 */
export const exportToCsv = async (data: any[], filename: string): Promise<void> => {
  if (!data || data.length === 0) {
    console.warn('No data provided for CSV export.');
    return;
  }

  try {
    const csv = Papa.unparse(data);
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    if (link.download !== undefined) {
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', `${filename}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } else {
        // Fallback for older browsers or specific security settings
        (window.navigator as any).msSaveBlob(blob, `${filename}.csv`);
    }
  } catch (error) {
    console.error('Error exporting to CSV:', error);
    throw error;
  }
};

/**
 * Exports data to a PDF file and triggers a download.
 * This is a basic example; PDF generation often requires complex document definitions.
 * @param data - Data to be included in the PDF. Structure depends on how you want to represent it.
 * @param filename - The name of the PDF file (without .pdf extension).
 */
export const exportToPdf = async (data: any, filename: string): Promise<void> => {
  try {
    // Example: Simple PDF document definition.
    // You'll need to create a more sophisticated structure based on your data.
    // `data` might be an object containing various pieces of information for the dashboard.
    // The `exportUtils.ts` would typically transform this `data` into a pdfMake document definition.
    const documentDefinition: any = {
      content: [
        { text: `Report: ${filename.replace(/-/g, ' ')}`, style: 'header' },
        { text: `Generated on: ${new Date().toLocaleDateString()}`, style: 'subheader' },
        // Example: Displaying data as a simple list or table
        // This part needs to be highly customized based on the `data` structure.
        { text: 'Data Overview:', style: 'subheader', margin: [0, 20, 0, 10] },
        { text: JSON.stringify(data, null, 2), style: 'code' } // Basic representation
      ],
      styles: {
        header: {
          fontSize: 18,
          bold: true,
          margin: [0, 0, 0, 10] as [number, number, number, number],
        },
        subheader: {
          fontSize: 14,
          bold: true,
          margin: [0, 10, 0, 5] as [number, number, number, number],
        },
        code: {
          font: 'Courier', // Make sure Courier is available or use a default like Roboto
        }
      },
      defaultStyle: {
        // font: 'Roboto' // Ensure this font is loaded if specified
      }
    };

    pdfMake.createPdf(documentDefinition).download(`${filename}.pdf`);
  } catch (error) {
    console.error('Error exporting to PDF:', error);
    throw error;
  }
};