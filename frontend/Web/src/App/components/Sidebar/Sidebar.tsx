import { Avatar, Box, IconButton, Tooltip, Typography } from '@mui/material';

import NavIcon, { type NavIconName } from '../icon/NavIcon';

export type AdminPageId = 'description' | 'instruction';

type SidebarUser = {
  email?: string;
};

type SidebarProps = {
  active: AdminPageId;
  onChange: (page: AdminPageId) => void;
  user: SidebarUser | null;
  onLogout: () => void;
};

const Sidebar = ({ active, onChange, user, onLogout }: SidebarProps) => {
  const items: { id: AdminPageId; label: string; icon: NavIconName }[] = [
    { id: 'description', label: 'Описание приложения', icon: 'description' },
    { id: 'instruction', label: 'Инструкция', icon: 'instruction' },
  ];

  return (
    <Box
      sx={{
        width: 264,
        flexShrink: 0,
        height: '100vh',
        position: 'sticky',
        top: 0,
        backgroundColor: '#fff',
        borderRight: '1px solid rgba(0,0,0,0.06)',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: '2px 0 16px rgba(0,0,0,0.03)',
      }}
    >
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          px: 2.5,
          py: 2.25,
          borderBottom: '1px solid rgba(0,0,0,0.06)',
        }}
      >
        <Box
          sx={{
            width: 36,
            height: 36,
            borderRadius: '10px',
            background: 'rgb(255,182,39)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <NavIcon name="logo" />
        </Box>
        <Box>
          <Typography sx={{ fontWeight: 700, fontSize: 16, lineHeight: 1.1 }}>BeeIoT</Typography>
          <Typography
            sx={{
              fontSize: 11,
              color: 'rgba(0,0,0,0.5)',
              letterSpacing: '0.04em',
              textTransform: 'uppercase',
            }}
          >
            Admin Panel
          </Typography>
        </Box>
      </Box>

      <Box sx={{ p: 1.5, flex: 1 }}>
        <Typography
          sx={{
            fontSize: 11,
            fontWeight: 600,
            color: 'rgba(0,0,0,0.4)',
            letterSpacing: '0.08em',
            px: 1.5,
            py: 1,
          }}
        >
          КОНТЕНТ ПРИЛОЖЕНИЯ
        </Typography>
        {items.map((item) => {
          const isActive = active === item.id;

          return (
            <Box
              key={item.id}
              onClick={() => onChange(item.id)}
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1.5,
                px: 1.5,
                py: 1.25,
                mb: 0.5,
                borderRadius: '10px',
                cursor: 'pointer',
                color: isActive ? '#000' : 'rgba(0,0,0,0.7)',
                backgroundColor: isActive ? 'rgba(255,182,39,0.18)' : 'transparent',
                fontWeight: isActive ? 600 : 500,
                position: 'relative',
                transition: 'background-color 120ms ease, color 120ms ease',
                '&:hover': {
                  backgroundColor: isActive ? 'rgba(255,182,39,0.22)' : 'rgba(0,0,0,0.04)',
                },
                '&::before': isActive
                  ? {
                      content: '""',
                      position: 'absolute',
                      left: -12,
                      top: 8,
                      bottom: 8,
                      width: 3,
                      borderRadius: 4,
                      backgroundColor: 'rgb(255,182,39)',
                    }
                  : {},
              }}
            >
              <NavIcon name={item.icon} />
              <Typography sx={{ fontSize: 14, fontWeight: 'inherit' }}>{item.label}</Typography>
            </Box>
          );
        })}
      </Box>

      <Box
        sx={{
          p: 2,
          borderTop: '1px solid rgba(0,0,0,0.06)',
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
        }}
      >
        <Avatar
          sx={{
            width: 36,
            height: 36,
            fontSize: 14,
            fontWeight: 600,
            bgcolor: 'rgb(255,182,39)',
            color: '#000',
          }}
        >
          {(user?.email?.[0] || 'A').toUpperCase()}
        </Avatar>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography
            sx={{
              fontSize: 13,
              fontWeight: 600,
              lineHeight: 1.2,
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >
            {user?.email || 'admin@beeiot.app'}
          </Typography>
          <Typography sx={{ fontSize: 11, color: 'rgba(0,0,0,0.5)' }}>Администратор</Typography>
        </Box>
        <Tooltip title="Выйти">
          <IconButton
            size="small"
            onClick={onLogout}
            sx={{
              color: 'rgba(0,0,0,0.55)',
              '&:hover': { color: 'rgb(192,0,0)', bgcolor: 'rgba(192,0,0,0.08)' },
            }}
          >
            <NavIcon name="logout" />
          </IconButton>
        </Tooltip>
      </Box>
    </Box>
  );
};

export default Sidebar;
