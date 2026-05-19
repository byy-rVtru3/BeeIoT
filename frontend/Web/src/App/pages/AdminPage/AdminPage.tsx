import { Box } from '@mui/material';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';

import Sidebar, { type AdminPageId } from '@/App/components/Sidebar';
import { useAuthStore } from '@/store/useAuthStore';

const AdminPage = () => {
  const user = useAuthStore((state) => state.user);
  const signOut = useAuthStore((state) => state.signOut);
  const location = useLocation();
  const navigate = useNavigate();

  const active: AdminPageId = location.pathname.includes('/admin/instruction')
    ? 'instruction'
    : 'description';

  const handleChange = (page: AdminPageId) => {
    navigate(`/admin/${page}`);
  };

  const handleLogout = () => {
    signOut();
    navigate('/auth', { replace: true });
  };

  return (
    <Box
      sx={{
        display: 'flex',
        minHeight: '100vh',
        backgroundColor: 'background.default',
      }}
    >
      <Sidebar active={active} onChange={handleChange} user={user} onLogout={handleLogout} />

      <Box
        component="main"
        sx={{
          flex: 1,
          minWidth: 0,
          px: { xs: 2, md: 4 },
          py: { xs: 3, md: 4 },
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};

export default AdminPage;
