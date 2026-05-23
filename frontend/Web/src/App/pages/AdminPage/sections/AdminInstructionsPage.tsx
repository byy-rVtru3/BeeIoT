import ChevronRightIcon from '@mui/icons-material/ChevronRight';
import { Box, Divider, Paper, Stack, Typography } from '@mui/material';
import { useState } from 'react';

import { PrimaryButton, SettingsButton, UnderlineTextField } from '@/components/ui';

const AdminInstructionsPage = () => {
  const [title, setTitle] = useState('');
  const [summary, setSummary] = useState('');
  const [content, setContent] = useState('');

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Инструкции</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Создавайте инструкции для администраторов и пчеловодов.
        </Typography>
      </Box>

      <Paper
        elevation={0}
        sx={{
          p: { xs: 3, md: 4 },
          borderRadius: 3,
          border: '1px solid',
          borderColor: 'divider',
          boxShadow: 'none',
        }}
      >
        <Stack spacing={2}>
          <UnderlineTextField
            value={title}
            onChange={setTitle}
            placeholder="Заголовок инструкции"
          />
          <UnderlineTextField
            value={summary}
            onChange={setSummary}
            placeholder="Короткое описание"
          />
          <UnderlineTextField
            value={content}
            onChange={setContent}
            placeholder="Текст инструкции"
            multiline
            minRows={4}
          />
          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <PrimaryButton fullWidth={false} sx={{ px: 4, py: 1.5 }}>
              Сохранить
            </PrimaryButton>
          </Box>
        </Stack>
      </Paper>

      <Paper
        elevation={0}
        sx={{
          p: { xs: 3, md: 4 },
          borderRadius: 3,
          border: '1px solid',
          borderColor: 'divider',
          boxShadow: 'none',
        }}
      >
        <Typography variant="subtitle1" sx={{ mb: 2 }}>
          Черновики
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <Stack spacing={1.5}>
          <SettingsButton endIcon={<ChevronRightIcon />}>Как подготовить улей</SettingsButton>
          <SettingsButton endIcon={<ChevronRightIcon />}>Зимняя консервация</SettingsButton>
          <SettingsButton endIcon={<ChevronRightIcon />}>Проверка датчиков</SettingsButton>
        </Stack>
      </Paper>
    </Stack>
  );
};

export default AdminInstructionsPage;
