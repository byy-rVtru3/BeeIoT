import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { Box, Paper, Stack, Typography } from '@mui/material';
import { useState } from 'react';

import { PrimaryButton, SettingsButton, UnderlineTextField } from '@/components/ui';

const AdminHivesPage = () => {
  const [name, setName] = useState('');
  const [location, setLocation] = useState('');
  const [topic, setTopic] = useState('');

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Ульи</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Добавляйте ульи и связывайте их с MQTT топиками.
        </Typography>
      </Box>

      <Paper
        sx={{
          p: { xs: 3, md: 4 },
          borderRadius: 3,
          border: '1px solid',
          borderColor: 'divider',
        }}
      >
        <Stack spacing={2}>
          <UnderlineTextField value={name} onChange={setName} placeholder="Название улья" />
          <UnderlineTextField value={location} onChange={setLocation} placeholder="Локация" />
          <UnderlineTextField value={topic} onChange={setTopic} placeholder="MQTT топик" />
          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <PrimaryButton fullWidth={false} sx={{ px: 4, py: 1.5 }}>
              Добавить
            </PrimaryButton>
          </Box>
        </Stack>
      </Paper>

      <Paper
        sx={{
          p: { xs: 3, md: 4 },
          borderRadius: 3,
          border: '1px solid',
          borderColor: 'divider',
        }}
      >
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          Активные ульи
        </Typography>
        <Stack spacing={1.5}>
          <SettingsButton endIcon={<ChevronRightIcon />}>Hive Aurora</SettingsButton>
          <SettingsButton endIcon={<ChevronRightIcon />}>Hive North-12</SettingsButton>
          <SettingsButton endIcon={<ChevronRightIcon />}>Hive Atlas</SettingsButton>
        </Stack>
      </Paper>
    </Stack>
  );
};

export default AdminHivesPage;
