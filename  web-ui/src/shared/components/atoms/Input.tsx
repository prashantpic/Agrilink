import React from 'react';
import { TextField as MuiTextField, TextFieldProps as MuiTextFieldProps } from '@mui/material';

// Extend MuiTextFieldProps if you need to add custom props
export interface InputProps extends MuiTextFieldProps {
  // Add custom props here, e.g.:
  // customVariant?: 'rounded' | 'flat';
}

const Input: React.FC<InputProps> = ({
  variant = 'outlined', // Default Material-UI variant
  size = 'small',       // Default size
  ...rest
}) => {
  return (
    <MuiTextField
      variant={variant}
      size={size}
      {...rest}
    />
  );
};

export default Input;