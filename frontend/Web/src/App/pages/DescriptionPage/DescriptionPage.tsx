import { zodResolver } from '@hookform/resolvers/zod';
import { Alert as DescAlert, Box as DescBox, Paper as DescPaper, Snackbar } from '@mui/material';
import { useState as descUseState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';

import PageHeader, { type PageHeaderStatus } from '../../components/PageHeader';

import FieldBlock from './FieldBlock';

const descriptionSchema = z.object({
  title: z.string().min(1, 'Введите заголовок').max(80, 'Максимум 80 символов'),
  short: z.string().min(1, 'Введите краткое описание').max(160, 'Максимум 160 символов'),
  full: z.string().min(1, 'Введите полное описание').max(2000, 'Максимум 2000 символов'),
});

type DescriptionData = z.infer<typeof descriptionSchema>;

type SnackbarState = null | {
  severity: 'success' | 'error' | 'info' | 'warning';
  text: string;
};

const DEFAULT_DESCRIPTION: DescriptionData = {
  title: 'BeeIoT — мониторинг ульев',
  short: 'Контролируйте здоровье пасеки в реальном времени.',
  full: `BeeIoT — это система IoT-мониторинга пчелиных ульев. Подключите хаб с датчиками и получайте телеметрию по температуре, шуму и весу улья прямо в телефон.

Ведите учёт маток, фиксируйте выполненные работы, получайте уведомления о критических событиях. Всё, что нужно пасечнику, — в одном приложении.`,
};

const DescriptionPage = () => {
  const [saving, setSaving] = descUseState(false);
  const [snack, setSnack] = descUseState<SnackbarState>(null);

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<DescriptionData>({
    resolver: zodResolver(descriptionSchema),
    defaultValues: DEFAULT_DESCRIPTION,
    mode: 'onBlur',
  });

  const titleValue = useWatch({ control, name: 'title' });
  const shortValue = useWatch({ control, name: 'short' });
  const fullValue = useWatch({ control, name: 'full' });

  const handleSave = handleSubmit((data) => {
    setSaving(true);
    setTimeout(() => {
      reset(data);
      setSaving(false);
      setSnack({ severity: 'success', text: 'Описание сохранено' });
    }, 600);
  });

  const status: PageHeaderStatus = {
    label: 'Опубликовано',
    bg: 'rgba(34,139,34,0.12)',
    fg: 'rgb(28,108,28)',
  };

  return (
    <DescBox>
      <PageHeader
        title="Описание приложения"
        subtitle="Тексты, которые видят пользователи на странице «О приложении»"
        status={status}
        onSave={handleSave}
        saving={saving}
        dirty={isDirty}
      />

      <DescPaper
        elevation={0}
        sx={{
          backgroundColor: '#fff',
          border: '1px solid rgba(0,0,0,0.06)',
          borderRadius: '16px',
          p: { xs: 2.5, sm: 4 },
          boxShadow: '0px 1px 2px rgba(0,0,0,0.03)',
        }}
      >
        <DescBox sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
          <Controller
            name="title"
            control={control}
            render={({ field }) => (
              <FieldBlock
                label="Заголовок"
                hint="Отображается крупным шрифтом в шапке экрана «О приложении»"
                value={field.value}
                onChange={field.onChange}
                counter={[titleValue.length, 80]}
                maxLength={80}
                errorText={errors.title?.message}
              />
            )}
          />
          <Controller
            name="short"
            control={control}
            render={({ field }) => (
              <FieldBlock
                label="Краткое описание"
                hint="Одна строка под заголовком — суть приложения"
                value={field.value}
                onChange={field.onChange}
                counter={[shortValue.length, 160]}
                maxLength={160}
                errorText={errors.short?.message}
              />
            )}
          />
          <Controller
            name="full"
            control={control}
            render={({ field }) => (
              <FieldBlock
                label="Полное описание"
                hint="Многострочный текст. Поддерживаются абзацы (Enter)"
                value={field.value}
                onChange={field.onChange}
                multiline
                rows={8}
                counter={[fullValue.length, 2000]}
                maxLength={2000}
                errorText={errors.full?.message}
              />
            )}
          />
        </DescBox>
      </DescPaper>

      <Snackbar
        open={Boolean(snack)}
        autoHideDuration={3000}
        onClose={() => setSnack(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        {snack ? (
          <DescAlert severity={snack.severity} variant="filled" sx={{ borderRadius: 2 }}>
            {snack.text}
          </DescAlert>
        ) : undefined}
      </Snackbar>
    </DescBox>
  );
};

export default DescriptionPage;
