import { zodResolver } from '@hookform/resolvers/zod';
import { Alert as DescAlert, Box as DescBox, Paper as DescPaper, Snackbar } from '@mui/material';
import { useEffect, useRef, useState as descUseState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';

import FullScreenLoader from '@/components/FullScreenLoader/FullScreenLoader';
import { useUpdateAppDescriptionMutation } from '@/hooks/mutations/useUpdateAppDescriptionMutation';
import { useAppDescriptionQuery } from '@/hooks/queries/useAppDescriptionQuery';
import type { AppDescription } from '@/types/appDescriptionType';

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

const EMPTY_DESCRIPTION: DescriptionData = {
  title: '',
  short: '',
  full: '',
};

const getErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error === 'object' && error !== null && 'message' in error) {
    return String((error as { message?: string }).message ?? fallback);
  }
  return fallback;
};

const DescriptionPage = () => {
  const [snack, setSnack] = descUseState<SnackbarState>(null);
  const lastLoadedRef = useRef<AppDescription | null | undefined>(undefined);

  const { data, isLoading, isError } = useAppDescriptionQuery();

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<DescriptionData>({
    resolver: zodResolver(descriptionSchema),
    defaultValues: EMPTY_DESCRIPTION,
    mode: 'onBlur',
  });

  const updateMutation = useUpdateAppDescriptionMutation({
    onSuccess: (next) => {
      reset({ title: next.title, short: next.short, full: next.full });
      setSnack({ severity: 'success', text: 'Описание сохранено' });
    },
    onError: (error) => {
      setSnack({
        severity: 'error',
        text: getErrorMessage(error, 'Не удалось сохранить описание'),
      });
    },
  });

  useEffect(() => {
    if (data === undefined || isDirty || lastLoadedRef.current === data) {
      return;
    }

    const next = data ?? EMPTY_DESCRIPTION;
    reset({ title: next.title, short: next.short, full: next.full });
    lastLoadedRef.current = data;
  }, [data, isDirty, reset]);

  const titleValue = useWatch({ control, name: 'title' });
  const shortValue = useWatch({ control, name: 'short' });
  const fullValue = useWatch({ control, name: 'full' });

  const handleSave = handleSubmit((formData) => updateMutation.mutate(formData));

  const saving = updateMutation.isPending;

  const hasDescription = Boolean(data);
  const notFound = data === null;
  const isReady = data !== undefined;

  const status: PageHeaderStatus = hasDescription
    ? {
        label: 'Опубликовано',
        bg: 'rgba(34,139,34,0.12)',
        fg: 'rgb(28,108,28)',
      }
    : {
        label: 'Черновик',
        bg: 'rgba(255,159,67,0.16)',
        fg: 'rgb(183,96,0)',
      };

  if (isLoading && !isReady) {
    return <FullScreenLoader />;
  }

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
          {notFound ? (
            <DescAlert severity="info" variant="outlined" sx={{ borderRadius: 2 }}>
              Описание еще не задано. Заполните поля и нажмите «Сохранить».
            </DescAlert>
          ) : null}
          {isError ? (
            <DescAlert severity="error" variant="outlined" sx={{ borderRadius: 2 }}>
              Не удалось загрузить описание. Проверьте соединение и попробуйте еще раз.
            </DescAlert>
          ) : null}
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
