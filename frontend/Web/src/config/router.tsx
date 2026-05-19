import { createBrowserRouter, Navigate } from 'react-router-dom';

import App from '@/App';
import AdminPage from '@/App/pages/AdminPage';
import DescriptionPage from '@/App/pages/DescriptionPage';
import InstructionPage from '@/App/pages/InstructionPage';
import NotFoundPage from '@/App/pages/NotFoundPage';
import FullScreenLoader from '@/components/FullScreenLoader/FullScreenLoader';
import RouterErrorBoundary from '@/components/RouterErrorBoundary/RouterErrorBoundary';

import { AuthRedirect, HomeRedirect, RequireAuth } from './routerGuards';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <App />,
    hydrateFallbackElement: <FullScreenLoader />,
    children: [
      { index: true, element: <HomeRedirect /> },
      { path: 'auth', element: <AuthRedirect /> },
      {
        path: 'admin',
        element: (
          <RequireAuth>
            <AdminPage />
          </RequireAuth>
        ),
        errorElement: <RouterErrorBoundary />,
        children: [
          { index: true, element: <Navigate to="description" replace /> },
          { path: 'description', element: <DescriptionPage /> },
          { path: 'instruction', element: <InstructionPage /> },
        ],
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);
