import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { routePaths } from './routePaths';
import ProtectedRoute from '../auth/ProtectedRoute';
import LoginPage from '../../pages/LoginPage';
import NotFoundPage from '../../pages/NotFoundPage';
import ForbiddenPage from '../../pages/ForbiddenPage';
import FarmerDashboardPage from '../../pages/FarmerDashboardPage';
import AdminDashboardPage from '../../pages/AdminDashboardPage';
import FarmPlotAdminDashboardPage from '../../pages/FarmPlotAdminDashboardPage';
import ConsultantDashboardPage from '../../pages/ConsultantDashboardPage';
import { ROLES } from '../config/roleConfig'; // Import ROLES directly
import { useAuth } from '../../features/auth/hooks/useAuth';

const AppRoutes = () => {
  const { isAuthenticated, user } = useAuth();

  // Determine default path after login based on role
  const getDefaultDashboardPath = () => {
    if (!isAuthenticated || !user?.role) return routePaths.LOGIN;
    switch (user.role) {
      case ROLES.FARMER:
        return routePaths.DASHBOARD_FARMER;
      case ROLES.ADMIN:
        return routePaths.DASHBOARD_ADMIN;
      case ROLES.FARM_PLOT_ADMIN:
        return routePaths.DASHBOARD_FARM_PLOT_ADMIN;
      case ROLES.CONSULTANT:
        return routePaths.DASHBOARD_CONSULTANT;
      default:
        return routePaths.LOGIN;
    }
  };

  return (
    <Routes>
      <Route path={routePaths.LOGIN} element={<LoginPage />} />
      <Route path={routePaths.NOT_FOUND} element={<NotFoundPage />} />
      <Route path={routePaths.FORBIDDEN} element={<ForbiddenPage />} />

      {/* Redirect root to login or appropriate dashboard */}
      <Route 
        path="/" 
        element={
          isAuthenticated ? <Navigate to={getDefaultDashboardPath()} replace /> : <Navigate to={routePaths.LOGIN} replace />
        } 
      />

      {/* Protected Dashboard Routes */}
      <Route element={<ProtectedRoute allowedRoles={[ROLES.FARMER]} />}>
        <Route path={routePaths.DASHBOARD_FARMER} element={<FarmerDashboardPage />} />
        {/* Add other farmer-specific routes here under the same ProtectedRoute if they share the same role access */}
      </Route>
      <Route element={<ProtectedRoute allowedRoles={[ROLES.ADMIN]} />}>
        <Route path={routePaths.DASHBOARD_ADMIN} element={<AdminDashboardPage />} />
      </Route>
      <Route element={<ProtectedRoute allowedRoles={[ROLES.FARM_PLOT_ADMIN]} />}>
        <Route path={routePaths.DASHBOARD_FARM_PLOT_ADMIN} element={<FarmPlotAdminDashboardPage />} />
      </Route>
      <Route element={<ProtectedRoute allowedRoles={[ROLES.CONSULTANT]} />}>
        <Route path={routePaths.DASHBOARD_CONSULTANT} element={<ConsultantDashboardPage />} />
      </Route>

      {/* Catch all other routes not defined */}
      <Route path="*" element={<Navigate to={routePaths.NOT_FOUND} replace />} />
    </Routes>
  );
};

export default AppRoutes;