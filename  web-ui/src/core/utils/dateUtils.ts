import { format, parse, isValid, parseISO } from 'date-fns';

/**
 * Formats a date object, timestamp, or ISO string into a specified string format.
 * @param date The date to format (Date object, number timestamp, or ISO string).
 * @param formatString The desired output format string (e.g., 'yyyy-MM-dd HH:mm:ss').
 * @returns The formatted date string, or an empty string if the date is invalid.
 */
export const formatDate = (
  date: Date | number | string | undefined | null,
  formatString: string = 'yyyy-MM-dd'
): string => {
  if (!date) return '';
  try {
    const dateObj = typeof date === 'string' ? parseISO(date) : new Date(date);
    if (!isValid(dateObj)) {
      // Try parsing with a common format if parseISO fails and it's a string
      if (typeof date === 'string') {
        const parsed = parse(date, 'yyyy-MM-dd HH:mm:ss', new Date());
        if (isValid(parsed)) return format(parsed, formatString);
        const parsedShort = parse(date, 'yyyy-MM-dd', new Date());
        if (isValid(parsedShort)) return format(parsedShort, formatString);
      }
      return ''; // Or throw error, or return a default string like 'Invalid Date'
    }
    return format(dateObj, formatString);
  } catch (error) {
    console.error('Error formatting date:', date, error);
    return ''; // Or rethrow, or return default
  }
};

/**
 * Parses a date string with a given format into a Date object.
 * @param dateString The date string to parse.
 * @param formatString The format of the input date string.
 * @returns A Date object, or null if parsing fails.
 */
export const parseDateString = (
  dateString: string | undefined | null,
  formatString: string = 'yyyy-MM-dd'
): Date | null => {
  if (!dateString) return null;
  try {
    const parsedDate = parse(dateString, formatString, new Date());
    return isValid(parsedDate) ? parsedDate : null;
  } catch (error) {
    console.error('Error parsing date string:', dateString, error);
    return null;
  }
};

/**
 * Converts a date to an ISO 8601 string.
 * @param date The date to convert.
 * @returns ISO string or empty string if invalid.
 */
export const toISOString = (date: Date | number | undefined | null): string => {
    if (!date) return '';
    try {
        const dateObj = new Date(date);
        if (!isValid(dateObj)) return '';
        return dateObj.toISOString();
    } catch (error) {
        console.error('Error converting to ISO string:', date, error);
        return '';
    }
};

// Add other common date utility functions as needed
// e.g., differenceInDays, addDays, isBefore, isAfter, etc.
// export { differenceInDays, addDays, isBefore, isAfter } from 'date-fns';