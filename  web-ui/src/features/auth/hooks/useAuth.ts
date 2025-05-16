import { useCallback } from 'react';
import { useAppSelector, useAppDispatch } from '../../../core/store/hooks'; // Assuming typed hooks
import { loginUser, logoutUser, AuthState, LoginPayload, initializeAuth } from '../store/authSlice';
import { User } from '../../../types/user';

interface UseAuthReturn extends AuthState {
  login: (credentials: LoginPayload) => Promise<any>; // Thunk returns a Promise
  logout: () => Promise<any>; // Thunk returns a Promise
  initAuth: () => Promise<any>;
  // Add other actions or selectors if needed
}

export const useAuth = (): UseAuthReturn => {
  const authState = useAppSelector((state) => state.auth); // Assuming 'auth' is the slice name in rootReducer
  const dispatch = useAppDispatch();

  const login = useCallback(
    (credentials: LoginPayload) => {
      return dispatch(loginUser(credentials));
    },
    [dispatch]
  );

  const logout = useCallback(() => {
    return dispatch(logoutUser());
  }, [dispatch]);

  const initAuth = useCallback(() => {
    return dispatch(initializeAuth());
  }, [dispatch]);


  return {
    ...authState,
    login,
    logout,
    initAuth,
  };
};