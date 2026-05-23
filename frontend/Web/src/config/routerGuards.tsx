import { type ReactNode } from 'react';
import { Navigate } from 'react-router-dom';

import LoginPage from '@/App/pages/LoginPage';
import { useAuthStore } from '@/store/useAuthStore';

export const RequireAuth = ({ children }: { children: ReactNode }) => {
  const token = useAuthStore((state) => state.token);
  if (!token) {
    return <Navigate to="/auth" replace />;
  }
  return children;
};

export const HomeRedirect = () => {
  const token = useAuthStore((state) => state.token);
  return <Navigate to={token ? '/admin/description' : '/auth'} replace />;
};

export const AuthRedirect = () => {
  const token = useAuthStore((state) => state.token);
  return token ? <Navigate to="/admin/description" replace /> : <LoginPage />;
};
