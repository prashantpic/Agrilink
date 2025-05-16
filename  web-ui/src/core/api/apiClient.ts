import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { appConfig } from '../config/appConfig';
import { store } from '../store';
import { logout } from '../../features/auth/store/authSlice';
import { routePaths } from '../routing/routePaths';

const apiClient = axios.create({
  baseURL: appConfig.API_GW_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = store.getState().auth.token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response) {
      const { status } = error.response;
      if (status === 401) {
        // Unauthorized: dispatch logout and redirect to login
        // Check if already on login page to prevent redirect loop if token becomes invalid on login page itself
        if (window.location.pathname !== routePaths.LOGIN) {
          store.dispatch(logout());
          // Full page reload to clear any residual state and ensure clean redirect
          window.location.href = routePaths.LOGIN;
        }
      } else if (status === 403) {
        // Forbidden: redirect to forbidden page
        // Avoid redirect loop if already on forbidden page
        if (window.location.pathname !== routePaths.FORBIDDEN) {
           window.location.href = routePaths.FORBIDDEN;
        }
      }
      // Potentially log other errors or show generic notifications
    } else if (error.request) {
      // The request was made but no response was received
      console.error('API Error: No response received', error.request);
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error('API Error: Request setup failed', error.message);
    }
    return Promise.reject(error);
  }
);

export default apiClient;