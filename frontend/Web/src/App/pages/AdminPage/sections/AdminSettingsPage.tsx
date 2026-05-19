import { Box, Paper, Stack, Typography } from '@mui/material';
import { useState } from 'react';

import { PrimaryButton, UnderlineTextField } from '@/components/ui';

const AdminSettingsPage = () => {
  const [smtpHost, setSmtpHost] = useState('smtp.beeiot.io');
  const [smtpUser, setSmtpUser] = useState('admin@beeiot.io');
  const [mqttBroker, setMqttBroker] = useState('mqtt.beeiot.io');

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Настройки</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Базовые параметры инфраструктуры и уведомлений.
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
          <UnderlineTextField value={smtpHost} onChange={setSmtpHost} placeholder="SMTP host" />
          <UnderlineTextField value={smtpUser} onChange={setSmtpUser} placeholder="SMTP user" />
          <UnderlineTextField
            value={mqttBroker}
            onChange={setMqttBroker}
            placeholder="MQTT broker"
          />
          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <PrimaryButton fullWidth={false} sx={{ px: 4, py: 1.5 }}>
              Сохранить
            </PrimaryButton>
          </Box>
        </Stack>
      </Paper>
    </Stack>
  );
};

export default AdminSettingsPage;
