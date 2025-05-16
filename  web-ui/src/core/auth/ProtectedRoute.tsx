import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../../features/auth/hooks/useAuth';
import { routePaths } from '../routing/routePaths';
import { UserRole } from '../../types/user'; // Assuming UserRole is defined here

interface ProtectedRouteProps {
  allowedRoles?: UserRole[];
  children?: React.ReactNode;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ allowedRoles, children }) => {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    // Redirect them to the /login page, but save the current location they were
    // trying to go to when they were redirected. This allows us to send them
    // along to that page after they login, which is a nicer user experience
    // than dropping them off on the home page.
    return <Navigate to={routePaths.LOGIN} state={{ from: location }} replace />;
  }

  if (allowedRoles && user?.role && !allowedRoles.includes(user.role)) {
    // User is authenticated but does not have the required role
    return <Navigate to={routePaths.FORBIDDEN} state={{ from: location }} replace />;
  }
  
  // If no allowedRoles are specified, just being authenticated is enough.
  // Or if roles are specified and user has one of them.
  return children ? <>{children}</> : <Outlet />;
};

export default ProtectedRoute;