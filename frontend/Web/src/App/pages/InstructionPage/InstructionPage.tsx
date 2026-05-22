import { zodResolver } from '@hookform/resolvers/zod';
import {
  Alert as InsAlert,
  Box as InsBox,
  Button as InsButton,
  Paper as InsPaper,
  Snackbar as InsSnackbar,
  TextField as InsTextField,
  Typography as InsTypography,
} from '@mui/material';
import { useState as insUseState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { z } from 'zod';

import IconPlus from '@/App/components/icon/IconPlus';
import FullScreenLoader from '@/components/FullScreenLoader/FullScreenLoader';
import { useCreateInstructionMutation } from '@/hooks/mutations/useCreateInstructionMutation';
import { useDeleteInstructionMutation } from '@/hooks/mutations/useDeleteInstructionMutation';
import { useInstructionsQuery } from '@/hooks/queries/useInstructionsQuery';

import PageHeader, { type PageHeaderStatus } from '../../components/PageHeader';

import InstructionItem, { type InstructionItemData } from './InstructionItem';

type SnackbarState = null | {
  severity: 'success' | 'error' | 'info' | 'warning';
  text: string;
};

const instructionSchema = z.object({
  title: z.string().min(1, 'Введите заголовок').max(100, 'Максимум 100 символов'),
  body: z.string().min(1, 'Введите текст').max(1000, 'Максимум 1000 символов'),
});

type InstructionFormData = z.infer<typeof instructionSchema>;

const InstructionPage = () => {
  const [openIds, setOpenIds] = insUseState<Set<string>>(new Set());
  const [snack, setSnack] = insUseState<SnackbarState>(null);

  const { data, isLoading, isError } = useInstructionsQuery();

  const createMutation = useCreateInstructionMutation({
    onSuccess: () => {
      reset();
      setSnack({ severity: 'success', text: 'Пункт добавлен' });
    },
    onError: () => {
      setSnack({ severity: 'error', text: 'Не удалось добавить пункт' });
    },
  });

  const deleteMutation = useDeleteInstructionMutation({
    onSuccess: () => {
      setSnack({ severity: 'success', text: 'Пункт удалён' });
    },
    onError: () => {
      setSnack({ severity: 'error', text: 'Не удалось удалить пункт' });
    },
  });

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isValid },
  } = useForm<InstructionFormData>({
    resolver: zodResolver(instructionSchema),
    defaultValues: {
      title: '',
      body: '',
    },
    mode: 'onChange',
  });

  const items: InstructionItemData[] = (data ?? []).map((item) => ({
    id: item.id,
    title: item.title,
    body: item.body,
    open: openIds.has(item.id),
  }));

  const updateItem = () => undefined;

  const toggleItem = (id: string) =>
    setOpenIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });

  const deleteItem = (id: string) => {
    if (!confirm('Удалить этот пункт?')) return;
    setOpenIds((prev) => {
      const next = new Set(prev);
      next.delete(id);
      return next;
    });
    deleteMutation.mutate(id);
  };

  const expandAll = () => setOpenIds(new Set((data ?? []).map((item) => item.id)));

  const collapseAll = () => setOpenIds(new Set());

  const handleCreate = handleSubmit((values) => {
    createMutation.mutate({
      title: values.title,
      body: values.body,
    });
  });

  const status: PageHeaderStatus = {
    label: `${items.length} ${pluralize(items.length, ['пункт', 'пункта', 'пунктов'])}`,
    bg: 'rgba(255,182,39,0.18)',
    fg: 'rgb(120,80,0)',
  };

  if (isLoading) {
    return <FullScreenLoader />;
  }

  return (
    <InsBox>
      <PageHeader
        title="Инструкция"
        subtitle="Аккордеон на экране «Как пользоваться приложением»"
        status={status}
      />

      <InsPaper
        elevation={0}
        sx={{
          backgroundColor: '#fff',
          border: '1.5px solid rgba(0,0,0,0.08)',
          borderRadius: '16px',
          p: { xs: 2.5, sm: 3.5 },
          boxShadow: 'none',
        }}
      >
        <InsBox component="form" onSubmit={handleCreate} sx={{ mb: 3 }}>
          <InsBox sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <InsTypography sx={{ fontSize: 14, fontWeight: 600 }}>Новый пункт</InsTypography>
            <Controller
              name="title"
              control={control}
              render={({ field }) => (
                <InsTextField
                  {...field}
                  fullWidth
                  size="small"
                  placeholder="Заголовок инструкции"
                  error={Boolean(errors.title)}
                  helperText={errors.title?.message ?? ' '}
                  slotProps={{
                    htmlInput: {
                      maxLength: 100,
                    },
                  }}
                />
              )}
            />
            <Controller
              name="body"
              control={control}
              render={({ field }) => (
                <InsTextField
                  {...field}
                  fullWidth
                  multiline
                  minRows={3}
                  maxRows={8}
                  placeholder="Текст инструкции"
                  error={Boolean(errors.body)}
                  helperText={errors.body?.message ?? ' '}
                  slotProps={{
                    htmlInput: {
                      maxLength: 1000,
                    },
                  }}
                />
              )}
            />
            <InsBox sx={{ display: 'flex', justifyContent: 'flex-end' }}>
              <InsButton
                type="submit"
                variant="outlined"
                startIcon={<IconPlus />}
                disabled={!isValid || createMutation.isPending}
                sx={ghostBtnSx}
              >
                {createMutation.isPending ? 'Добавление…' : 'Добавить пункт'}
              </InsButton>
            </InsBox>
          </InsBox>
        </InsBox>

        {isError ? (
          <InsAlert severity="error" sx={{ borderRadius: 2, mb: 2 }}>
            Не удалось загрузить список инструкций
          </InsAlert>
        ) : null}

        <InsBox
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 2,
            mb: 2.5,
            flexWrap: 'wrap',
          }}
        >
          <InsTypography sx={{ fontSize: 13, color: 'rgba(0,0,0,0.55)' }}>
            Список инструкций
          </InsTypography>
          <InsBox sx={{ display: 'flex', gap: 1 }}>
            <InsButton variant="outlined" size="small" onClick={expandAll} sx={ghostBtnSx}>
              Раскрыть все
            </InsButton>
            <InsButton variant="outlined" size="small" onClick={collapseAll} sx={ghostBtnSx}>
              Свернуть все
            </InsButton>
          </InsBox>
        </InsBox>

        <InsBox sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          {items.map((item, idx) => (
            <InstructionItem
              key={item.id}
              item={item}
              index={idx}
              total={items.length}
              onUpdate={updateItem}
              onDelete={deleteItem}
              onMove={() => null}
              onToggle={toggleItem}
              isDragging={false}
              isOver={false}
              dragPos={null}
              onDragStart={() => null}
              onDragEnd={() => null}
              onDragOver={() => null}
              onDragLeave={() => null}
              onDrop={() => null}
              readOnly
            />
          ))}
          {items.length === 0 ? (
            <InsBox
              sx={{
                py: 6,
                textAlign: 'center',
                border: '2px dashed rgba(0,0,0,0.12)',
                borderRadius: '14px',
                color: 'rgba(0,0,0,0.5)',
              }}
            >
              <InsTypography sx={{ fontSize: 14, mb: 1 }}>Нет ни одного пункта</InsTypography>
              <InsTypography sx={{ fontSize: 12 }}>
                Добавьте первый пункт инструкции, чтобы начать
              </InsTypography>
            </InsBox>
          ) : null}
        </InsBox>
      </InsPaper>

      <InsSnackbar
        open={Boolean(snack)}
        autoHideDuration={3000}
        onClose={() => setSnack(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        {snack ? (
          <InsAlert severity={snack.severity} variant="filled" sx={{ borderRadius: 2 }}>
            {snack.text}
          </InsAlert>
        ) : undefined}
      </InsSnackbar>
    </InsBox>
  );
};

const ghostBtnSx = {
  borderRadius: '10px',
  borderColor: 'rgba(0,0,0,0.15)',
  color: 'rgba(0,0,0,0.7)',
  fontSize: 12,
  fontWeight: 600,
  textTransform: 'none',
  px: 1.5,
  '&:hover': {
    borderColor: 'rgb(255,182,39)',
    bgcolor: 'rgba(255,182,39,0.08)',
    color: '#000',
  },
};

const pluralize = (value: number, forms: [string, string, string]) => {
  const a = Math.abs(value) % 100;
  const b = a % 10;
  if (a > 10 && a < 20) return forms[2];
  if (b > 1 && b < 5) return forms[1];
  if (b === 1) return forms[0];
  return forms[2];
};

export default InstructionPage;
