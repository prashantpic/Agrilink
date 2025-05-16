import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { authApiService, LoginPayload, AuthResponse } from '../services/authApiService';
import { User, UserRole } from '../../../types/user'; // Assuming User and UserRole types are defined

interface AuthState {
  isAuthenticated: boolean;
  user: User | null;
  token: string | null;
  loading: 'idle' | 'pending' | 'succeeded' | 'failed';
  error: string | null | unknown; // Allow for unknown error types
}

const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  token: authApiService.getStoredAuthToken(), // Initialize token from localStorage
  loading: 'idle',
  error: null,
};

// Async thunk for login
export const loginUser = createAsyncThunk<
  AuthResponse, // Return type of the payload creator
  LoginPayload,   // First argument to the payload creator
  { rejectValue: string } // Types for ThunkAPI
>(
  'auth/loginUser',
  async (credentials: LoginPayload, { rejectWithValue }) => {
    try {
      const response = await authApiService.login(credentials);
      return response; // This will be action.payload in the fulfilled reducer
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || 'Login failed';
      return rejectWithValue(errorMessage);
    }
  }
);

// Async thunk for logout
export const logoutUser = createAsyncThunk(
  'auth/logoutUser',
  async (_, { dispatch }) => {
    await authApiService.logout();
    // Dispatch additional cleanup actions if needed
    dispatch(clearAuth()); // Dispatch local clearAuth action
  }
);

// Async thunk to initialize auth state from stored token (e.g., on app load)
export const initializeAuth = createAsyncThunk<
  { user: User | null, token: string },
  void,
  { rejectValue: string }
>(
  'auth/initializeAuth',
  async (_, { rejectWithValue, dispatch }) => {
    const token = authApiService.getStoredAuthToken();
    if (token) {
      try {
        // Option 1: Decode token client-side to get user info (if it's a JWT with user claims)
        // This is faster but less secure and might not have all user details.
        // const decodedUser = decodeToken(token); // Placeholder for a token decoding function

        // Option 2: Call an API endpoint to validate token and get user profile
        // This is more secure and ensures user data is fresh.
        // const user = await authApiService.fetchUserProfile(); // Assuming this fetches user by validating token
        // For now, we'll assume if a token exists, we need to fetch user profile.
        // If fetchUserProfile is not implemented or not desired at this stage,
        // we might set isAuthenticated to true and token, but user to null until a protected route is hit
        // or a profile is explicitly fetched.

        // For this example, let's assume fetchUserProfile is not fetching full User object,
        // and user object is mainly set upon login.
        // If token is present, we consider user "potentially" authenticated.
        // A proper implementation would fetch the user profile using the token.
        // For now, if token exists, we will set isAuthenticated to true.
        // The actual user object will be populated upon successful login or profile fetch.
        
        // If your token contains user role directly (e.g., JWT claims):
        // For simplicity, let's mock a user object if a token is found.
        // In a real app, you'd fetch this from an API or decode the token.
        // This part needs to be robust in a real application.
        // const user: User = { id: 'temp-id', username: 'User', role: UserRole.FARMER }; // Mock user
        // For now, just set token and isAuthenticated. User profile will be fetched by a protected route or on demand.
        
        // A better approach for initializeAuth if fetchUserProfile is implemented:
        const userProfile = await authApiService.fetchUserProfile(); // This needs to exist
        if (userProfile) {
             return { user: userProfile, token };
        } else {
            // Token exists but user profile fetch failed (e.g. token expired/invalid)
            dispatch(logoutUser()); // Clean up invalid token
            return rejectWithValue('Invalid token or failed to fetch profile.');
        }

      } catch (error: any) {
        dispatch(logoutUser()); // Clean up on error
        return rejectWithValue(error.message || 'Failed to initialize auth');
      }
    }
    return rejectWithValue('No token found'); // No token, nothing to initialize
  }
);


const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setAuthLoading: (state, action: PayloadAction<'idle' | 'pending'>) => {
        state.loading = action.payload;
    },
    clearAuthError: (state) => {
        state.error = null;
    },
    // Local action to clear auth state, used by logoutUser thunk or directly
    clearAuth: (state) => {
      state.isAuthenticated = false;
      state.user = null;
      state.token = null;
      state.loading = 'idle';
      state.error = null;
    },
    // Direct way to set token if needed (e.g., from OAuth redirect)
    setToken: (state, action: PayloadAction<string>) => {
        state.token = action.payload;
        localStorage.setItem(authApiService.getStoredAuthToken()!, action.payload); // Assuming getStoredAuthToken returns the key name
        // Potentially decode token here and set user and isAuthenticated if token is valid
        // state.isAuthenticated = true; // This needs careful handling
    }
  },
  extraReducers: (builder) => {
    builder
      // Login User
      .addCase(loginUser.pending, (state) => {
        state.loading = 'pending';
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action: PayloadAction<AuthResponse>) => {
        state.loading = 'succeeded';
        state.isAuthenticated = true;
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.error = null;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = 'failed';
        state.isAuthenticated = false;
        state.user = null;
        state.token = null;
        state.error = action.payload || action.error.message || 'Unknown login error';
      })
      // Logout User
      .addCase(logoutUser.pending, (state) => {
        state.loading = 'pending'; // Optional: show loading state during logout
      })
      .addCase(logoutUser.fulfilled, (state) => {
        // State clearing is handled by the clearAuth action dispatched within the thunk
        // Or can be done directly here if clearAuth is not dispatched in thunk
        state.isAuthenticated = false;
        state.user = null;
        state.token = null;
        state.loading = 'idle';
        state.error = null;
      })
      .addCase(logoutUser.rejected, (state, action) => {
        // Even if server logout fails, client should still log out
        state.isAuthenticated = false;
        state.user = null;
        state.token = null;
        state.loading = 'idle';
        state.error = action.error.message || 'Logout failed but client state cleared.';
      })
      // Initialize Auth
      .addCase(initializeAuth.pending, (state) => {
        state.loading = 'pending';
      })
      .addCase(initializeAuth.fulfilled, (state, action: PayloadAction<{ user: User | null, token: string }>) => {
        state.token = action.payload.token;
        state.user = action.payload.user;
        state.isAuthenticated = !!action.payload.user; // True if user object is returned
        state.loading = 'succeeded';
        state.error = null;
      })
      .addCase(initializeAuth.rejected, (state, action) => {
        // Handled by dispatching logoutUser or directly clearing state
        state.isAuthenticated = false;
        state.user = null;
        state.token = null;
        state.loading = 'failed';
        state.error = action.payload || action.error.message || 'Auth initialization failed.';
      });
  },
});

export const { setAuthLoading, clearAuthError, clearAuth, setToken } = authSlice.actions;

export default authSlice.reducer;