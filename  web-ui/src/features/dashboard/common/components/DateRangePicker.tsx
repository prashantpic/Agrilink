import React, { useState } from 'react';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { Box, TextField, Button } from '@mui/material';
import { DateRange as DateRangeType } from '../types/dashboardTypes';

interface DateRangePickerProps {
  initialDateRange?: DateRangeType;
  onDateRangeChange: (dateRange: DateRangeType) => void;
  maxDate?: Date;
  minDate?: Date;
}

const DateRangePicker: React.FC<DateRangePickerProps> = ({
  initialDateRange,
  onDateRangeChange,
  maxDate = new Date(),
  minDate,
}) => {
  const [startDate, setStartDate] = useState<Date | null>(initialDateRange?.start || null);
  const [endDate, setEndDate] = useState<Date | null>(initialDateRange?.end || null);

  const handleStartDateChange = (date: Date | null) => {
    setStartDate(date);
  };

  const handleEndDateChange = (date: Date | null) => {
    setEndDate(date);
  };

  const handleApply = () => {
    if (startDate && endDate && startDate > endDate) {
      // Handle error: start date cannot be after end date
      alert('Start date cannot be after end date.'); // Replace with a proper notification
      return;
    }
    onDateRangeChange({ start: startDate, end: endDate });
  };

  const handleClear = () => {
    setStartDate(null);
    setEndDate(null);
    onDateRangeChange({ start: null, end: null });
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
        <DatePicker
          label="Start Date"
          value={startDate}
          onChange={handleStartDateChange}
          maxDate={endDate || maxDate}
          minDate={minDate}
          slots={{textField: (params) => <TextField {...params} size="small" />}}
        />
        <DatePicker
          label="End Date"
          value={endDate}
          onChange={handleEndDateChange}
          minDate={startDate || minDate}
          maxDate={maxDate}
          slots={{textField: (params) => <TextField {...params} size="small" />}}
        />
        <Button variant="contained" onClick={handleApply} size="medium">
          Apply
        </Button>
        <Button variant="outlined" onClick={handleClear} size="medium">
          Clear
        </Button>
      </Box>
    </LocalizationProvider>
  );
};

export default DateRangePicker;