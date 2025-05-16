import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, TextField, Alert, CircularProgress } from '@mui/material';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import Button from '../../../shared/components/atoms/Button'; // Assuming Button atom exists
// import FormField from '../../../shared/components/molecules/FormField'; // Assuming FormField molecule exists
import { useAppDispatch } from '../../../core/store/hooks'; // Assuming typed hooks
import { loginUser } from '../store/authSlice'; // Assuming loginUser thunk from authSlice
import { routePaths } from '../../../core/routing/routePaths'; // Assuming routePaths
import { User, UserRole } from '../../../types/user'; // Assuming User type

// Define LoginPayload if not already defined in authTypes.ts
interface LoginPayload {
  identifier: string; // username or phone
  password?: string;  // for username/password
  otp?: string;       // for phone/otp
  loginType: 'credentials' | 'otp';
}

const validationSchema = Yup.object({
  identifier: Yup.string().required('Username or Phone is required'),
  password: Yup.string().when('loginType', {
    is: 'credentials',
    then: (schema) => schema.required('Password is required'),
    otherwise: (schema) => schema.optional(),
  }),
  otp: Yup.string().when('loginType', {
    is: 'otp',
    then: (schema) => schema.required('OTP is required').length(6, 'OTP must be 6 digits'),
    otherwise: (schema) => schema.optional(),
  }),
});


const LoginForm: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [loginType, setLoginType] = useState<'credentials' | 'otp'>('credentials');
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);

  const formik = useFormik<LoginPayload & { passwordType?: 'password' | 'otp' }>({
    initialValues: {
      identifier: '',
      password: '',
      otp: '',
      loginType: 'credentials',
    },
    validationSchema: validationSchema,
    onSubmit: async (values) => {
      setIsLoading(true);
      setError(null);
      try {
        const payload: LoginPayload = {
          identifier: values.identifier,
          loginType: values.loginType,
        };
        if (values.loginType === 'credentials') {
          payload.password = values.password;
        } else {
          payload.otp = values.otp;
        }

        // The loginUser thunk should handle setting isAuthenticated and user in Redux state
        // and navigating upon successful login.
        const resultAction = await dispatch(loginUser(payload));

        if (loginUser.fulfilled.match(resultAction)) {
          const user = resultAction.payload.user as User; // Cast to User type
          // Navigate to the appropriate dashboard based on role
          // This logic might be better placed in AppRoutes or a dedicated redirect component after login
          switch (user.role) {
            case UserRole.FARMER:
              navigate(routePaths.DASHBOARD_FARMER);
              break;
            case UserRole.ADMIN:
              navigate(routePaths.DASHBOARD_ADMIN);
              break;
            case UserRole.FARM_PLOT_ADMIN:
                navigate(routePaths.DASHBOARD_FPA);
                break;
            case UserRole.CONSULTANT:
                navigate(routePaths.DASHBOARD_CONSULTANT);
                break;
            default:
              navigate('/'); // Fallback to a default route
          }
        } else if (loginUser.rejected.match(resultAction)) {
          setError(resultAction.payload as string || 'Login failed. Please check your credentials.');
        }
      } catch (err: any) {
        setError(err.message || 'An unexpected error occurred.');
      } finally {
        setIsLoading(false);
      }
    },
  });

  const toggleLoginType = () => {
    const newType = loginType === 'credentials' ? 'otp' : 'credentials';
    setLoginType(newType);
    formik.setFieldValue('loginType', newType);
    formik.setFieldValue('password', '');
    formik.setFieldValue('otp', '');
    formik.setErrors({}); // Clear errors on type switch
    setError(null);
  };


  return (
    <Box
      component="form"
      onSubmit={formik.handleSubmit}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: 2,
        p: 3,
        border: '1px solid #ccc',
        borderRadius: 2,
        maxWidth: 400,
        margin: 'auto',
      }}
    >
      <TextField
        fullWidth
        id="identifier"
        name="identifier"
        label={loginType === 'credentials' ? "Username or Email" : "Phone Number"}
        value={formik.values.identifier}
        onChange={formik.handleChange}
        onBlur={formik.handleBlur}
        error={formik.touched.identifier && Boolean(formik.errors.identifier)}
        helperText={formik.touched.identifier && formik.errors.identifier}
        disabled={isLoading}
      />

      {loginType === 'credentials' && (
        <TextField
          fullWidth
          id="password"
          name="password"
          label="Password"
          type="password"
          value={formik.values.password}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={formik.touched.password && Boolean(formik.errors.password)}
          helperText={formik.touched.password && formik.errors.password}
          disabled={isLoading}
        />
      )}

      {loginType === 'otp' && (
         <TextField
          fullWidth
          id="otp"
          name="otp"
          label="OTP"
          type="text"
          value={formik.values.otp}
          onChange={formik.handleChange}
          onBlur={formik.handleBlur}
          error={formik.touched.otp && Boolean(formik.errors.otp)}
          helperText={formik.touched.otp && formik.errors.otp}
          disabled={isLoading}
        />
      )}
      
      {error && <Alert severity="error" sx={{ width: '100%' }}>{error}</Alert>}

      <Button
        color="primary"
        variant="contained"
        fullWidth
        type="submit"
        disabled={isLoading || !formik.isValid}
        startIcon={isLoading ? <CircularProgress size={20} color="inherit" /> : null}
      >
        {isLoading ? 'Logging in...' : 'Login'}
      </Button>

      <Button
        variant="text"
        onClick={toggleLoginType}
        disabled={isLoading}
      >
        {loginType === 'credentials' ? 'Login with OTP' : 'Login with Password'}
      </Button>
    </Box>
  );
};

export default LoginForm;