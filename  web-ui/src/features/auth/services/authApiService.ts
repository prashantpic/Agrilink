import apiClient from '../../../core/api/apiClient'; // Assuming apiClient is configured
import { API_ENDPOINTS } from '../../../core/api/apiEndpoints'; // Assuming apiEndpoints
import { appConfig } from '../../../core/config/appConfig'; // Assuming appConfig for token key
import { User } from '../../../types/user'; // Assuming User type definition

// Define types for login payload and auth response if not already in a shared types file
export interface LoginPayload {
  identifier: string; // Can be username, email, or phone
  password?: string;  // For credential-based login
  otp?: string;       // For OTP-based login
  loginType: 'credentials' | 'otp'; // To differentiate login method
}

export interface AuthResponse {
  token: string;
  user: User; // Includes id, username, role(s)
  // refreshToken?: string; // Optional refresh token
}

const AUTH_TOKEN_KEY = appConfig.AUTH_TOKEN_STORAGE_KEY || 'authToken'; // Use key from appConfig

const login = async (credentials: LoginPayload): Promise<AuthResponse> => {
  try {
    // Adapt endpoint based on loginType if backend requires different endpoints
    const response = await apiClient.post<AuthResponse>(API_ENDPOINTS.AUTH.LOGIN, credentials);
    if (response.data && response.data.token) {
      localStorage.setItem(AUTH_TOKEN_KEY, response.data.token);
      // apiClient might automatically set the token for subsequent requests via interceptor
    }
    return response.data;
  } catch (error) {
    // apiClient's interceptor might handle some errors globally
    // Re-throw or handle specific login errors here
    console.error('Login API error:', error);
    throw error;
  }
};

const logout = async (): Promise<void> => {
  try {
    // Call backend logout endpoint if it exists (e.g., to invalidate session/token server-side)
    // await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT); // Uncomment if backend endpoint exists
  } catch (error) {
    console.error('Logout API error (ignorable if only client-side cleanup):', error);
    // Generally, client-side logout should proceed even if server call fails
  } finally {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    // Clear any other auth-related persisted state
    // apiClient interceptors might need to clear the token from its internal config too, or it will pick it up from store which will be cleared.
  }
};

// Function to get the currently stored token (e.g., for initializing auth state on app load)
const getStoredAuthToken = (): string | null => {
  return localStorage.getItem(AUTH_TOKEN_KEY);
};

// Function to get user profile (if token exists and is valid - typically called after app load)
const fetchUserProfile = async (): Promise<User | null> => {
    // This assumes an endpoint like /auth/me or /users/me to get profile with existing token
    // The token should be automatically added by apiClient's request interceptor
    if (!getStoredAuthToken()) return null; // No token, no profile to fetch

    try {
        // const response = await apiClient.get<{ user: User }>(API_ENDPOINTS.AUTH.PROFILE); // Assuming endpoint like /auth/profile
        // return response.data.user;

        // Placeholder: If no dedicated profile endpoint, the user object from login is the source.
        // For re-hydration, the token is primary. User details might be re-fetched or come from token claims if simple.
        // For this example, we'll assume user profile is part of the login response and stored in Redux.
        // This function would be more relevant if we need to validate token and get fresh user data.
        console.warn("fetchUserProfile: No dedicated profile endpoint assumed for now. User data comes from login.");
        return null; // Or implement actual fetch if /auth/me endpoint exists
    } catch (error) {
        console.error('Fetch user profile error:', error);
        // If token is invalid (e.g., 401), logout might be triggered by apiClient interceptor
        return null;
    }
};


export const authApiService = {
  login,
  logout,
  getStoredAuthToken,
  fetchUserProfile,
  // Potentially other auth-related services: register, forgotPassword, resetPassword, verifyOtp, etc.
};