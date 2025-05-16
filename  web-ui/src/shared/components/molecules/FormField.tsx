```typescript
import React from 'react';
import { TextField, TextFieldProps, FormControl, FormHelperText, InputLabel } from '@mui/material';
// Assuming Input.tsx is a wrapper around MuiTextField or similar.
// For simplicity, this example uses MuiTextField directly.
// If `Input` atom exists, replace `TextField` with `<Input/>`.

type FormFieldProps = TextFieldProps & {
  label: string;
  errorText?: string;
};

const FormField: React.FC<FormFieldProps> = ({ label, errorText, error, helperText, ...rest }) => {
  return (
    <FormControl fullWidth error={error || !!errorText}>
      {/* Using TextField directly which includes InputLabel behavior */}
      <TextField
        label={label}
        error={error || !!errorText}
        helperText={errorText || helperText}
        variant="outlined" // Default variant, can be overridden by props
        {...rest}
      />
    </FormControl>
  );
};

export default FormField;
```