import {
  Alert as InsAlert,
  Box as InsBox,
  Button as InsButton,
  Paper as InsPaper,
  Snackbar as InsSnackbar,
  Typography as InsTypography,
} from '@mui/material';
import { useQueryClient } from '@tanstack/react-query';
import type { DragEvent } from 'react';
import { useEffect, useRef, useState as insUseState } from 'react';

import IconDrag from '@/App/components/icon/IconDrag';
import IconPlus from '@/App/components/icon/IconPlus';
import {
  createInstructionItem,
  deleteInstructionItem,
  reorderInstructionItems,
  updateInstructionItem,
} from '@/api/instructions';
import FullScreenLoader from '@/components/FullScreenLoader/FullScreenLoader';
import { useInstructionsQuery } from '@/hooks/queries/useInstructionsQuery';
import type { InstructionItem as InstructionApiItem } from '@/types/instructionType';

import PageHeader, { type PageHeaderStatus } from '../../components/PageHeader';

import InstructionItem, { type DragPosition, type InstructionItemData } from './InstructionItem';

type SnackbarState = null | {
  severity: 'success' | 'error' | 'info' | 'warning';
  text: string;
};

type InstructionSnapshot = Pick<InstructionItemData, 'id' | 'title' | 'body' | 'numbered'>;

type DragOverInfo = {
  id: string | null;
  pos: DragPosition;
};

const getErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error === 'object' && error !== null && 'message' in error) {
    return String((error as { message?: string }).message ?? fallback);
  }
  return fallback;
};

const toSnapshot = (items: InstructionItemData[]): InstructionSnapshot[] =>
  items.map(({ id, title, body, numbered }) => ({ id, title, body, numbered }));

const normalizePositions = (items: InstructionItemData[]): InstructionItemData[] =>
  items.map((item, index) => ({ ...item, position: index + 1 }));

const mapServerItems = (
  items: InstructionApiItem[],
  openMap: Map<string, boolean>
): InstructionItemData[] =>
  items
    .slice()
    .sort((a, b) => a.position - b.position)
    .map((item) => ({
      id: item.id,
      title: item.title,
      body: item.body,
      numbered: item.numbered,
      position: item.position,
      open: openMap.get(item.id) ?? false,
    }));

const createTempId = () => `temp-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;

const InstructionPage = () => {
  const [snack, setSnack] = insUseState<SnackbarState>(null);
  const [items, setItems] = insUseState<InstructionItemData[]>([]);
  const [initialItems, setInitialItems] = insUseState<InstructionSnapshot[]>([]);
  const [deletedIds, setDeletedIds] = insUseState<string[]>([]);
  const [saving, setSaving] = insUseState(false);
  const [draggingId, setDraggingId] = insUseState<string | null>(null);
  const [overInfo, setOverInfo] = insUseState<DragOverInfo>({ id: null, pos: null });

  const listEndRef = useRef<HTMLDivElement | null>(null);
  const itemsRef = useRef<InstructionItemData[]>([]);
  const dirtyRef = useRef(false);
  const lastDataUpdatedAtRef = useRef(0);
  const queryClient = useQueryClient();

  const { data, dataUpdatedAt, isLoading, isError } = useInstructionsQuery();

  useEffect(() => {
    itemsRef.current = items;
  }, [items]);

  const dirty =
    deletedIds.length > 0 || JSON.stringify(toSnapshot(items)) !== JSON.stringify(initialItems);

  useEffect(() => {
    dirtyRef.current = dirty;
  }, [dirty]);

  useEffect(() => {
    if (data === undefined) {
      return;
    }
    if (dirtyRef.current) {
      return;
    }
    if (dataUpdatedAt === lastDataUpdatedAtRef.current) {
      return;
    }

    lastDataUpdatedAtRef.current = dataUpdatedAt;
    const openMap = new Map(itemsRef.current.map((item) => [item.id, item.open]));
    const nextItems = mapServerItems(data, openMap);

    setItems(nextItems);
    setInitialItems(toSnapshot(nextItems));
    setDeletedIds([]);
  }, [data, dataUpdatedAt]);

  const expandAll = () => setItems((prev) => prev.map((item) => ({ ...item, open: true })));

  const collapseAll = () => setItems((prev) => prev.map((item) => ({ ...item, open: false })));

  const addItem = () => {
    const nextItem: InstructionItemData = {
      id: createTempId(),
      title: '',
      body: '',
      numbered: true,
      position: items.length + 1,
      open: true,
      isNew: true,
    };

    setItems((prev) => normalizePositions([...prev, nextItem]));
    setTimeout(() => listEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 100);
  };

  const updateItem = (id: string, patch: Partial<InstructionItemData>) => {
    setItems((prev) => prev.map((item) => (item.id === id ? { ...item, ...patch } : item)));
  };

  const toggleItem = (id: string) => {
    setItems((prev) => prev.map((item) => (item.id === id ? { ...item, open: !item.open } : item)));
  };

  const moveItem = (id: string, dir: number) => {
    setItems((prev) => {
      const index = prev.findIndex((item) => item.id === id);
      if (index < 0) return prev;

      const targetIndex = index + dir;
      if (targetIndex < 0 || targetIndex >= prev.length) return prev;

      const next = [...prev];
      const [moved] = next.splice(index, 1);
      next.splice(targetIndex, 0, moved);

      return normalizePositions(next);
    });
  };

  const deleteItem = (id: string) => {
    const target = items.find((item) => item.id === id);
    if (!target) return;
    if (!confirm('Удалить этот пункт?')) return;

    setItems((prev) => normalizePositions(prev.filter((item) => item.id !== id)));

    if (!target.isNew) {
      setDeletedIds((prev) => (prev.includes(id) ? prev : [...prev, id]));
    }
  };

  const handleDragStart = (event: DragEvent<HTMLDivElement>, id: string) => {
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', id);
    setDraggingId(id);
  };

  const handleDragEnd = () => {
    setDraggingId(null);
    setOverInfo({ id: null, pos: null });
  };

  const handleDragOver = (event: DragEvent<HTMLDivElement>, id: string) => {
    event.preventDefault();
    const rect = event.currentTarget.getBoundingClientRect();
    const pos: DragPosition = event.clientY - rect.top < rect.height / 2 ? 'before' : 'after';

    setOverInfo({ id, pos });
  };

  const handleDragLeave = (event: DragEvent<HTMLDivElement>) => {
    const related = event.relatedTarget as Node | null;
    if (related && event.currentTarget.contains(related)) return;

    setOverInfo({ id: null, pos: null });
  };

  const handleDrop = (event: DragEvent<HTMLDivElement>, id: string) => {
    event.preventDefault();
    const sourceId = draggingId ?? event.dataTransfer.getData('text/plain');
    if (!sourceId || sourceId === id) {
      handleDragEnd();
      return;
    }

    setItems((prev) => {
      const sourceIndex = prev.findIndex((item) => item.id === sourceId);
      const targetIndex = prev.findIndex((item) => item.id === id);
      if (sourceIndex < 0 || targetIndex < 0) return prev;

      const next = [...prev];
      const [moved] = next.splice(sourceIndex, 1);
      const withoutMoved = next;
      const targetIndexInNext = withoutMoved.findIndex((item) => item.id === id);

      const position = overInfo.id === id ? overInfo.pos : 'after';
      const insertIndex = position === 'before' ? targetIndexInNext : targetIndexInNext + 1;

      withoutMoved.splice(insertIndex, 0, moved);
      return normalizePositions(withoutMoved);
    });

    handleDragEnd();
  };

  const handleSave = async () => {
    if (!dirty) return;

    const trimmedItems = items.map((item) => ({
      ...item,
      title: item.title.trim(),
      body: item.body.trim(),
    }));

    if (trimmedItems.some((item) => !item.title || !item.body)) {
      setSnack({ severity: 'error', text: 'Заполните заголовок и текст всех пунктов' });
      return;
    }

    setSaving(true);

    try {
      const newItems = trimmedItems.filter((item) => item.isNew);
      const createdItems = await Promise.all(
        newItems.map((item) =>
          createInstructionItem({
            title: item.title,
            body: item.body,
            numbered: item.numbered,
            position: item.position,
          })
        )
      );

      let createdIndex = 0;
      const withIds = trimmedItems.map((item) => {
        if (!item.isNew) return item;
        const created = createdItems[createdIndex++];

        return {
          ...item,
          id: created.id,
          numbered: created.numbered,
          position: created.position,
          isNew: false,
        };
      });

      const initialMap = new Map(initialItems.map((item) => [item.id, item]));
      const updateTargets = withIds.filter((item) => {
        if (item.isNew || deletedIds.includes(item.id)) return false;
        const initial = initialMap.get(item.id);
        return (
          !initial ||
          initial.title !== item.title ||
          initial.body !== item.body ||
          initial.numbered !== item.numbered
        );
      });

      await Promise.all(
        updateTargets.map((item) =>
          updateInstructionItem(item.id, {
            title: item.title,
            body: item.body,
            numbered: item.numbered,
          })
        )
      );

      await Promise.all(deletedIds.map((id) => deleteInstructionItem(id)));

      await reorderInstructionItems({ order: withIds.map((item) => item.id) });

      const normalized = normalizePositions(withIds).map((item) => ({
        ...item,
        isNew: false,
      }));

      setItems(normalized);
      setInitialItems(toSnapshot(normalized));
      setDeletedIds([]);
      setSnack({ severity: 'success', text: 'Изменения сохранены' });

      await queryClient.invalidateQueries({ queryKey: ['instruction-items', 'admin'] });
    } catch (error) {
      setSnack({
        severity: 'error',
        text: getErrorMessage(error, 'Не удалось сохранить изменения'),
      });
    } finally {
      setSaving(false);
    }
  };

  const status: PageHeaderStatus = {
    label: `${items.length} ${pluralize(items.length, ['пункт', 'пункта', 'пунктов'])}`,
    bg: 'rgba(255,182,39,0.18)',
    fg: 'rgb(120,80,0)',
  };

  if (isLoading && data === undefined) {
    return <FullScreenLoader />;
  }

  return (
    <InsBox>
      <PageHeader
        title="Инструкция"
        subtitle="Аккордеон на экране «Как пользоваться приложением»"
        status={status}
        onSave={handleSave}
        saving={saving}
        dirty={dirty}
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
        {isError ? (
          <InsAlert severity="error" sx={{ borderRadius: 2, mb: 2 }}>
            Не удалось загрузить список инструкций
          </InsAlert>
        ) : null}

        <InsBox
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 1.25,
            mb: 2,
            color: 'rgba(0,0,0,0.55)',
          }}
        >
          <InsBox
            sx={{
              width: 28,
              height: 28,
              borderRadius: '8px',
              border: '1px dashed rgba(0,0,0,0.18)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <IconDrag />
          </InsBox>
          <InsTypography sx={{ fontSize: 12 }}>
            Перетаскивайте за иконку, чтобы менять порядок
          </InsTypography>
        </InsBox>

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
          <InsBox sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            <InsButton
              variant="outlined"
              size="small"
              onClick={addItem}
              startIcon={<IconPlus />}
              sx={ghostBtnSx}
            >
              Добавить пункт
            </InsButton>
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
              onMove={moveItem}
              onToggle={toggleItem}
              isDragging={draggingId === item.id}
              isOver={overInfo.id === item.id}
              dragPos={overInfo.id === item.id ? overInfo.pos : null}
              onDragStart={(event) => handleDragStart(event, item.id)}
              onDragEnd={handleDragEnd}
              onDragOver={(event) => handleDragOver(event, item.id)}
              onDragLeave={handleDragLeave}
              onDrop={(event) => handleDrop(event, item.id)}
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
              <InsTypography sx={{ fontSize: 12, mb: 2 }}>
                Добавьте первый пункт инструкции, чтобы начать
              </InsTypography>
              <InsButton
                variant="outlined"
                size="small"
                onClick={addItem}
                startIcon={<IconPlus />}
                sx={ghostBtnSx}
              >
                Добавить пункт
              </InsButton>
            </InsBox>
          ) : null}

          <InsBox ref={listEndRef} />
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
