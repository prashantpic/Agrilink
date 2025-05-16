import React from 'react';
import { Button as MuiButton, ButtonProps as MuiButtonProps, CircularProgress } from '@mui/material';

// Extend MuiButtonProps to include custom props if any
export interface ButtonProps extends MuiButtonProps {
  isLoading?: boolean;
  // Add other custom props here
  // example: customColor?: 'specialBlue' | 'urgentRed';
}

const Button: React.FC<ButtonProps> = ({
  children,
  isLoading = false,
  disabled,
  startIcon,
  ...rest
}) => {
  return (
    <MuiButton
      disabled={disabled || isLoading}
      startIcon={isLoading ? <CircularProgress size={20} color="inherit" /> : startIcon}
      {...rest}
    >
      {children}
    </MuiButton>
  );
};

export default Button;