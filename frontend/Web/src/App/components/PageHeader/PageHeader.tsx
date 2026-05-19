import { Box, Button, Chip, Typography } from '@mui/material';

export type PageHeaderStatus = {
  label: string;
  bg: string;
  fg: string;
};

type PageHeaderProps = {
  title: string;
  subtitle?: string;
  status?: PageHeaderStatus;
  onSave?: () => void;
  saving?: boolean;
  dirty?: boolean;
};

const PageHeader = ({
  title,
  subtitle,
  status,
  onSave,
  saving = false,
  dirty = false,
}: PageHeaderProps) => (
  <Box
    sx={{
      display: 'flex',
      alignItems: 'flex-start',
      justifyContent: 'space-between',
      gap: 2,
      mb: 3,
      flexWrap: 'wrap',
    }}
  >
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 0.5 }}>
        <Typography sx={{ fontSize: 26, fontWeight: 700, lineHeight: 1.2 }}>{title}</Typography>
        {status ? (
          <Chip
            size="small"
            label={status.label}
            sx={{
              bgcolor: status.bg,
              color: status.fg,
              fontWeight: 600,
              fontSize: 11,
              height: 22,
            }}
          />
        ) : null}
      </Box>
      {subtitle ? (
        <Typography sx={{ color: 'rgba(0,0,0,0.55)', fontSize: 14 }}>{subtitle}</Typography>
      ) : null}
    </Box>
    {onSave ? (
      <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center' }}>
        {dirty ? (
          <Typography sx={{ fontSize: 12, color: 'rgba(0,0,0,0.55)' }}>
            Есть несохранённые изменения
          </Typography>
        ) : null}
        <Button
          variant="outlined"
          onClick={onSave}
          disabled={saving || !dirty}
          sx={{
            minWidth: 140,
            py: 1.1,
            px: 2.5,
            borderRadius: '10px',
            borderWidth: '1.5px',
            borderColor: dirty ? 'rgb(255,182,39)' : 'rgba(0,0,0,0.15)',
            bgcolor: dirty ? 'rgb(255,182,39)' : '#fff',
            color: '#000',
            fontWeight: 600,
            '&:hover': {
              borderWidth: '1.5px',
              borderColor: 'rgb(228,147,19)',
              bgcolor: dirty ? 'rgb(228,147,19)' : 'rgba(255,182,39,0.08)',
            },
            '&.Mui-disabled': {
              borderWidth: '1.5px',
              borderColor: 'rgba(0,0,0,0.1)',
              bgcolor: 'rgba(0,0,0,0.03)',
              color: 'rgba(0,0,0,0.35)',
            },
          }}
        >
          {saving ? 'Сохранение…' : 'Сохранить'}
        </Button>
      </Box>
    ) : null}
  </Box>
);

export default PageHeader;
