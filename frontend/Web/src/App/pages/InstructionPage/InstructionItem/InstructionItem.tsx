import {
  Box as InsBox,
  Collapse,
  IconButton as InsIconButton,
  TextField as InsTextField,
  Tooltip as InsTooltip,
  Typography as InsTypography,
} from '@mui/material';
import type { DragEvent } from 'react';

import IconArrow from '@/App/components/icon/IconArrow';
import IconBullet from '@/App/components/icon/IconBullet';
import IconDrag from '@/App/components/icon/IconDrag';
import IconExpand from '@/App/components/icon/IconExpand';
import IconTrash from '@/App/components/icon/IconTrash';

type InstructionItemData = {
  id: number;
  title: string;
  body: string;
  open: boolean;
};

type DragPosition = 'before' | 'after' | null;

type InstructionItemProps = {
  item: InstructionItemData;
  index: number;
  total: number;
  onUpdate: (id: number, patch: Partial<InstructionItemData>) => void;
  onDelete: (id: number) => void;
  onMove: (id: number, dir: number) => void;
  onToggle: (id: number) => void;
  isDragging: boolean;
  isOver: boolean;
  dragPos: DragPosition;
  onDragStart: (event: DragEvent<HTMLDivElement>) => void;
  onDragEnd: () => void;
  onDragOver: (event: DragEvent<HTMLDivElement>) => void;
  onDragLeave: (event: DragEvent<HTMLDivElement>) => void;
  onDrop: (event: DragEvent<HTMLDivElement>) => void;
  readOnly?: boolean;
};

const outlineBtnSx = {
  border: '1.5px solid rgba(0,0,0,0.15)',
  borderRadius: '10px',
  width: 34,
  height: 34,
  color: 'rgba(0,0,0,0.7)',
  bgcolor: '#fff',
  transition: 'all 120ms ease',
  '&:hover': {
    borderColor: 'rgb(255,182,39)',
    color: '#000',
    bgcolor: 'rgba(255,182,39,0.08)',
  },
  '&.Mui-disabled': {
    borderColor: 'rgba(0,0,0,0.08)',
    color: 'rgba(0,0,0,0.25)',
    bgcolor: 'rgba(0,0,0,0.02)',
  },
};

const dangerBtnSx = {
  ...outlineBtnSx,
  '&:hover': {
    borderColor: 'rgb(192,0,0)',
    color: 'rgb(192,0,0)',
    bgcolor: 'rgba(192,0,0,0.06)',
  },
};

const InstructionItem = ({
  item,
  index,
  total,
  onUpdate,
  onDelete,
  onMove,
  onToggle,
  isDragging,
  isOver,
  dragPos,
  onDragStart,
  onDragEnd,
  onDragOver,
  onDragLeave,
  onDrop,
  readOnly = false,
}: InstructionItemProps) => {
  const titleLen = item.title.length;
  const bodyLen = item.body.length;

  return (
    <InsBox
      onDragOver={onDragOver}
      onDragLeave={onDragLeave}
      onDrop={onDrop}
      sx={{
        position: 'relative',
        border: '1.5px solid',
        borderColor: item.open ? 'rgb(255,182,39)' : 'rgba(0,0,0,0.12)',
        borderRadius: '14px',
        backgroundColor: '#fff',
        overflow: 'hidden',
        opacity: isDragging ? 0.4 : 1,
        transform: isDragging ? 'scale(0.98)' : 'none',
        transition:
          'border-color 150ms ease, box-shadow 150ms ease, opacity 150ms ease, transform 150ms ease',
        boxShadow: item.open ? '0 4px 18px rgba(255,182,39,0.12)' : 'none',
        '&::before':
          isOver && dragPos === 'before'
            ? {
                content: '""',
                position: 'absolute',
                left: 8,
                right: 8,
                top: -3,
                height: 4,
                borderRadius: 4,
                bgcolor: 'rgb(255,182,39)',
                boxShadow: '0 0 0 2px rgba(255,182,39,0.25)',
                zIndex: 2,
              }
            : {},
        '&::after':
          isOver && dragPos === 'after'
            ? {
                content: '""',
                position: 'absolute',
                left: 8,
                right: 8,
                bottom: -3,
                height: 4,
                borderRadius: 4,
                bgcolor: 'rgb(255,182,39)',
                boxShadow: '0 0 0 2px rgba(255,182,39,0.25)',
                zIndex: 2,
              }
            : {},
      }}
    >
      <InsBox
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1.25,
          px: 1.25,
          py: 1.25,
          '&:hover': { bgcolor: 'rgba(0,0,0,0.015)' },
        }}
      >
        <InsTooltip title="Перетащите, чтобы изменить порядок">
          <InsBox
            draggable={!readOnly}
            onDragStart={readOnly ? undefined : onDragStart}
            onDragEnd={readOnly ? undefined : onDragEnd}
            sx={{
              width: 34,
              height: 34,
              borderRadius: '10px',
              border: '1.5px solid rgba(0,0,0,0.12)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'rgba(0,0,0,0.45)',
              cursor: readOnly ? 'default' : 'grab',
              bgcolor: '#fff',
              transition: 'all 120ms ease',
              '&:hover': {
                borderColor: readOnly ? 'rgba(0,0,0,0.12)' : 'rgb(255,182,39)',
                color: readOnly ? 'rgba(0,0,0,0.45)' : '#000',
                bgcolor: readOnly ? '#fff' : 'rgba(255,182,39,0.08)',
              },
              '&:active': { cursor: readOnly ? 'default' : 'grabbing' },
            }}
          >
            <IconDrag />
          </InsBox>
        </InsTooltip>

        <IconBullet n={index + 1} />

        <InsBox
          onClick={() => onToggle(item.id)}
          sx={{ flex: 1, minWidth: 0, cursor: 'pointer', py: 0.5 }}
        >
          <InsTypography
            sx={{
              fontWeight: 600,
              fontSize: 15,
              color: item.title ? '#000' : 'rgba(0,0,0,0.4)',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >
            {item.title || 'Без названия'}
          </InsTypography>
        </InsBox>

        <InsBox sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
          <InsTooltip title="Вверх">
            <span>
              <InsIconButton
                disabled={readOnly || index === 0}
                onClick={() => onMove(item.id, -1)}
                sx={outlineBtnSx}
              >
                <IconArrow dir="up" />
              </InsIconButton>
            </span>
          </InsTooltip>
          <InsTooltip title="Вниз">
            <span>
              <InsIconButton
                disabled={readOnly || index === total - 1}
                onClick={() => onMove(item.id, 1)}
                sx={outlineBtnSx}
              >
                <IconArrow dir="down" />
              </InsIconButton>
            </span>
          </InsTooltip>
          <InsTooltip title="Удалить">
            <InsIconButton onClick={() => onDelete(item.id)} sx={dangerBtnSx}>
              <IconTrash />
            </InsIconButton>
          </InsTooltip>

          <InsTooltip title={item.open ? 'Свернуть' : 'Раскрыть'}>
            <InsIconButton
              onClick={() => onToggle(item.id)}
              sx={{
                ...outlineBtnSx,
                width: 34,
                height: 34,
                ...(item.open
                  ? {
                      borderColor: 'rgb(255,182,39)',
                      bgcolor: 'rgb(255,182,39)',
                      color: '#000',
                      '&:hover': {
                        borderColor: 'rgb(228,147,19)',
                        bgcolor: 'rgb(228,147,19)',
                        color: '#000',
                      },
                    }
                  : {}),
              }}
            >
              <IconExpand open={item.open} />
            </InsIconButton>
          </InsTooltip>
        </InsBox>
      </InsBox>

      <Collapse in={item.open} timeout={220}>
        <InsBox
          sx={{
            px: 2,
            pb: 2,
            pt: 0.5,
            borderTop: '1px dashed rgba(0,0,0,0.1)',
            display: 'flex',
            flexDirection: 'column',
            gap: 2,
            backgroundColor: 'rgba(255,182,39,0.04)',
          }}
        >
          <InsBox sx={{ pt: 2 }}>
            <InsBox sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.75 }}>
              <InsTypography sx={{ fontSize: 13, fontWeight: 600 }}>Заголовок пункта</InsTypography>
              <InsTypography
                sx={{
                  fontSize: 11,
                  color: titleLen > 90 ? 'rgb(192,0,0)' : 'rgba(0,0,0,0.45)',
                }}
              >
                {titleLen} / 100
              </InsTypography>
            </InsBox>
            <InsTextField
              fullWidth
              size="small"
              placeholder="Например: Как добавить улей"
              value={item.title}
              onChange={(event) => onUpdate(item.id, { title: event.target.value.slice(0, 100) })}
              slotProps={{
                htmlInput: {
                  readOnly,
                },
              }}
            />
          </InsBox>
          <InsBox>
            <InsBox sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.75 }}>
              <InsTypography sx={{ fontSize: 13, fontWeight: 600 }}>Текст пункта</InsTypography>
              <InsTypography
                sx={{
                  fontSize: 11,
                  color: bodyLen > 900 ? 'rgb(192,0,0)' : 'rgba(0,0,0,0.45)',
                }}
              >
                {bodyLen} / 1000
              </InsTypography>
            </InsBox>
            <InsTextField
              fullWidth
              multiline
              minRows={3}
              maxRows={10}
              placeholder="Подробно опишите шаг…"
              value={item.body}
              onChange={(event) => onUpdate(item.id, { body: event.target.value.slice(0, 1000) })}
              slotProps={{
                htmlInput: {
                  readOnly,
                },
              }}
            />
          </InsBox>
        </InsBox>
      </Collapse>
    </InsBox>
  );
};

export type { DragPosition, InstructionItemData, InstructionItemProps };

export default InstructionItem;
