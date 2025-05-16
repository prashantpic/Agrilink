import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
// Assuming authService.ts exists and provides these functions.
// For now, we'll define a placeholder for its expected interface.
// import * as authService from './authService';
// import { UserProfile } from '../../types'; // Assuming UserProfile type is defined elsewhere

// Placeholder for UserProfile type, replace with actual definition
interface UserProfile {
  id: string;
  username: string;
  email: string;
  role: string; // REQ-14-010: Role is important for conflict resolution access
  // other user properties
}

// Placeholder for authService functions, replace with actual implementation
const authService = {
  login: async (/* credentials */): Promise<{ token: string; user: UserProfile }> => {
    // Mock implementation
    console.log('AuthService: login called');
    await new Promise(resolve => setTimeout(resolve, 500));
    const mockUser: UserProfile = { id: 'user-123', username: 'testuser', email: 'test@example.com', role: 'Administrator' }; // Example role
    return { token: 'mock-jwt-token', user: mockUser };
  },
  logout: async (): Promise<void> => {
    // Mock implementation
    console.log('AuthService: logout called');
    await new Promise(resolve => setTimeout(resolve, 100));
  },
  getCurrentUser: async (token: string): Promise<UserProfile | null> => {
    // Mock implementation
    console.log('AuthService: getCurrentUser called with token', token);
    if (token === 'mock-jwt-token') {
      await new Promise(resolve => setTimeout(resolve, 300));
      return { id: 'user-123', username: 'testuser', email: 'test@example.com', role: 'Administrator' };
    }
    return null;
  },
  // Placeholder for secure token storage functions
  storeToken: async (token: string): Promise<void> => {
    console.log('SecureStorage: storing token', token);
    // In a real app, use react-native-keychain or similar
    // await Keychain.setGenericPassword('userToken', token);
    localStorage.setItem('userToken', token); // Fallback for web/easier mock
  },
  getToken: async (): Promise<string | null> => {
    console.log('SecureStorage: retrieving token');
    // return Keychain.getGenericPassword().then(creds => creds ? creds.password : null);
    return localStorage.getItem('userToken');
  },
  clearToken: async (): Promise<void> => {
    console.log('SecureStorage: clearing token');
    // await Keychain.resetGenericPassword();
    localStorage.removeItem('userToken');
  },
};


interface AuthState {
  token: string | null;
  user: UserProfile | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}

interface AuthContextType extends AuthState {
  login: (credentials: any) => Promise<void>; // Replace 'any' with actual credentials type
  logout: () => Promise<void>;
  checkAuthStatus: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [authState, setAuthState] = useState<AuthState>({
    token: null,
    user: null,
    isAuthenticated: false,
    isLoading: true,
    error: null,
  });

  const checkAuthStatus = async () => {
    setAuthState(prev => ({ ...prev, isLoading: true, error: null }));
    try {
      const token = await authService.getToken();
      if (token) {
        const user = await authService.getCurrentUser(token);
        if (user) {
          setAuthState({
            token,
            user,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } else {
          // Token might be invalid, clear it
          await authService.clearToken();
          setAuthState({ token: null, user: null, isAuthenticated: false, isLoading: false, error: 'Invalid session.' });
        }
      } else {
        setAuthState({ token: null, user: null, isAuthenticated: false, isLoading: false, error: null });
      }
    } catch (err: any) {
      console.error('Auth check failed:', err);
      setAuthState({ token: null, user: null, isAuthenticated: false, isLoading: false, error: err.message || 'Failed to check auth status' });
    }
  };

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const login = async (credentials: any) => {
    setAuthState(prev => ({ ...prev, isLoading: true, error: null }));
    try {
      const { token, user } = await authService.login(credentials);
      await authService.storeToken(token);
      setAuthState({
        token,
        user,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
    } catch (err: any) {
      console.error('Login failed:', err);
      setAuthState(prev => ({ ...prev, isLoading: false, error: err.message || 'Login failed' }));
      throw err; // Re-throw to allow UI to handle
    }
  };

  const logout = async () => {
    setAuthState(prev => ({ ...prev, isLoading: true, error: null }));
    try {
      await authService.logout(); // Call backend logout if necessary
      await authService.clearToken();
      setAuthState({
        token: null,
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: null,
      });
    } catch (err: any) {
      console.error('Logout failed:', err);
      setAuthState(prev => ({ ...prev, isLoading: false, error: err.message || 'Logout failed' }));
    }
  };

  return (
    <AuthContext.Provider value={{ ...authState, login, logout, checkAuthStatus }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};