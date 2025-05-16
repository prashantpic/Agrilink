import * as Keychain from 'react-native-keychain';
import apiClient from '../remote/apiClient';
import { API_BASE_URL, API_TOKEN_STORAGE_KEY } from '../../config/environment';

// Define a more specific User type if available, for now using a generic one
export interface User {
  id: string;
  username: string;
  email: string;
  role: string; // REQ-14-010: Role is crucial for conflict resolution access
  // Add other user properties as needed
}

export interface AuthResponse {
  token: string;
  user: User;
}

const AUTH_SERVICE_KEY = API_TOKEN_STORAGE_KEY || 'com.thesss.platform.mobile.authToken';

const authService = {
  login: async (credentials: {
    emailOrUsername?: string;
    password?: string;
    phone?: string; // Example: if login with phone OTP is supported
    otp?: string;
  }): Promise<User> => {
    try {
      // REQ-14-007: All API communication uses HTTPS (handled by apiClient)
      const response = await apiClient.post<AuthResponse>(`${API_BASE_URL}/auth/login`, credentials);
      const { token, user } = response.data;

      if (token && user) {
        // REQ-14-007: Securely store auth tokens
        await Keychain.setGenericPassword('userToken', token, { service: AUTH_SERVICE_KEY });
        // Store user details if needed, or rely on fetching them when token is present
        // For simplicity, we assume user object is returned and can be used immediately
        // and potentially stored in a context or state manager
        return user;
      } else {
        throw new Error('Login failed: Invalid token or user data received.');
      }
    } catch (error: any) {
      console.error('Login error:', error.response?.data?.message || error.message);
      throw error;
    }
  },

  logout: async (): Promise<void> => {
    try {
      // Optionally call a backend logout endpoint
      // await apiClient.post(`${API_BASE_URL}/auth/logout`);

      // REQ-14-007: Remove stored token
      await Keychain.resetGenericPassword({ service: AUTH_SERVICE_KEY });
      // Clear any user state in the app
    } catch (error: any) {
      console.error('Logout error:', error.response?.data?.message || error.message);
      // Decide if logout error should prevent local cleanup
      // For now, we ensure local cleanup even if server call fails
      await Keychain.resetGenericPassword({ service: AUTH_SERVICE_KEY });
    }
  },

  // REQ-14-010: Fetching user details including role
  getCurrentUser: async (): Promise<User | null> => {
    try {
      const credentials = await Keychain.getGenericPassword({ service: AUTH_SERVICE_KEY });
      if (!credentials || !credentials.password) {
        return null; // No token, no user
      }
      // Assuming a /me endpoint to get current user details based on token
      // The apiClient interceptor should add the token to the request
      const response = await apiClient.get<User>(`${API_BASE_URL}/users/me`);
      return response.data;
    } catch (error: any) {
      console.error('Get current user error:', error.response?.data?.message || error.message);
      // If fetching user fails (e.g. token expired), consider logging out
      // await authService.logout(); // Potentially trigger logout
      return null;
    }
  },

  getToken: async (): Promise<string | null> => {
    try {
      const credentials = await Keychain.getGenericPassword({ service: AUTH_SERVICE_KEY });
      return credentials ? credentials.password : null;
    } catch (error) {
      console.error('Error getting token from keychain:', error);
      return null;
    }
  },

  // Example: Registration function
  register: async (userData: any): Promise<User> => {
    try {
      const response = await apiClient.post<AuthResponse>(`${API_BASE_URL}/auth/register`, userData);
      const { token, user } = response.data;
      if (token && user) {
        await Keychain.setGenericPassword('userToken', token, { service: AUTH_SERVICE_KEY });
        return user;
      } else {
        throw new Error('Registration failed: Invalid token or user data received.');
      }
    } catch (error: any) {
      console.error('Registration error:', error.response?.data?.message || error.message);
      throw error;
    }
  },
};

export default authService;