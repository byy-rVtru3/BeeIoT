import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import {
  Box,
  ButtonBase,
  FormHelperText,
  IconButton,
  InputBase,
  Typography,
  useTheme,
} from '@mui/material';
import { useState, type ChangeEvent, type ReactNode } from 'react';

type UnderlineTextFieldProps = {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  error?: boolean;
  disabled?: boolean;
  name?: string;
  autoComplete?: string;
  type?: string;
  endAdornment?: ReactNode;
  multiline?: boolean;
  minRows?: number;
  maxRows?: number;
};

type ValidatedTextFieldProps = UnderlineTextFieldProps & {
  errorText?: string;
  supportingText?: string;
};

type PasswordTextFieldProps = Omit<ValidatedTextFieldProps, 'type' | 'endAdornment'>;

type OtpTextFieldProps = {
  value: string;
  onChange: (value: string) => void;
  length?: number;
  error?: boolean;
};

type ClickableProfileFieldProps = {
  label: string;
  value: string;
  onClick: () => void;
};

export const UnderlineTextField = ({
  value,
  onChange,
  placeholder,
  error = false,
  disabled = false,
  name,
  autoComplete,
  type = 'text',
  endAdornment,
  multiline = false,
  minRows,
  maxRows,
}: UnderlineTextFieldProps) => {
  const theme = useTheme();
  const inputTypography = theme.typography.caption;

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 0.5,
        minHeight: 52,
        paddingX: 1,
        paddingY: 1,
        borderBottom: '2px solid',
        borderColor: error ? 'error.main' : 'primary.main',
        transition: 'border-color 100ms ease',
        '&:focus-within': {
          borderColor: error ? 'error.main' : 'primary.main',
        },
        ...(disabled && {
          opacity: 0.6,
        }),
      }}
    >
      <InputBase
        fullWidth
        value={value}
        onChange={(event: ChangeEvent<HTMLInputElement>) => onChange(event.target.value)}
        placeholder={placeholder}
        disabled={disabled}
        name={name}
        autoComplete={autoComplete}
        type={type}
        multiline={multiline}
        minRows={minRows}
        maxRows={maxRows}
        sx={{
          color: 'text.primary',
          '& .MuiInputBase-input': {
            fontSize: inputTypography.fontSize,
            lineHeight: inputTypography.lineHeight ?? 1.4,
            fontWeight: inputTypography.fontWeight,
            fontFamily: inputTypography.fontFamily,
            padding: 0,
          },
          '& .MuiInputBase-input::placeholder': {
            color: 'text.primary',
            opacity: 0.5,
            fontSize: inputTypography.fontSize,
          },
        }}
      />
      {endAdornment ? (
        <Box sx={{ display: 'flex', alignItems: 'center', height: '100%' }}>{endAdornment}</Box>
      ) : null}
    </Box>
  );
};

export const ValidatedTextField = ({
  errorText,
  supportingText,
  ...props
}: ValidatedTextFieldProps) => {
  const helperText = errorText ?? supportingText ?? ' ';

  return (
    <Box>
      <UnderlineTextField {...props} error={Boolean(errorText)} />
      <FormHelperText
        error={Boolean(errorText)}
        sx={{ marginLeft: 0, marginRight: 0, minHeight: 18, marginTop: 0.5 }}
      >
        {helperText}
      </FormHelperText>
    </Box>
  );
};

export const PasswordTextField = ({
  errorText,
  supportingText,
  ...props
}: PasswordTextFieldProps) => {
  const [visible, setVisible] = useState(false);

  return (
    <ValidatedTextField
      {...props}
      errorText={errorText}
      supportingText={supportingText}
      type={visible ? 'text' : 'password'}
      endAdornment={
        <IconButton
          size="small"
          edge="end"
          onClick={() => setVisible((current) => !current)}
          aria-label={visible ? 'Hide password' : 'Show password'}
          sx={{ color: 'text.primary', opacity: 0.6 }}
        >
          {visible ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
        </IconButton>
      }
    />
  );
};

export const OtpTextField = ({ value, onChange, length = 6, error = false }: OtpTextFieldProps) => {
  const handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    const nextValue = event.target.value.replace(/\D/g, '').slice(0, length);
    onChange(nextValue);
  };

  return (
    <Box sx={{ position: 'relative' }}>
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: `repeat(${length}, 1fr)`,
          gap: 3,
        }}
      >
        {Array.from({ length }).map((_, index) => (
          <Box
            key={index}
            sx={{
              borderBottom: '2px solid',
              borderColor: error ? 'error.main' : 'primary.main',
              paddingTop: 2,
              paddingBottom: 1,
              textAlign: 'center',
              fontSize: 32,
              fontWeight: 500,
              lineHeight: 1.25,
              letterSpacing: '0em',
            }}
          >
            {value[index] ?? ''}
          </Box>
        ))}
      </Box>
      <InputBase
        value={value}
        onChange={handleChange}
        inputProps={{
          inputMode: 'numeric',
          pattern: '[0-9]*',
          maxLength: length,
          'aria-label': 'One-time password',
        }}
        sx={{
          position: 'absolute',
          inset: 0,
          opacity: 0,
          cursor: 'text',
          zIndex: 1,
        }}
      />
    </Box>
  );
};

export const ClickableProfileField = ({ label, value, onClick }: ClickableProfileFieldProps) => (
  <ButtonBase
    onClick={onClick}
    sx={{
      display: 'block',
      width: '100%',
      textAlign: 'left',
      paddingY: 0.5,
      borderBottom: '2px solid',
      borderColor: 'primary.main',
    }}
  >
    <Typography variant="body2" sx={{ marginBottom: 0.5, color: 'text.primary', opacity: 0.5 }}>
      {label}
    </Typography>
    <Typography variant="body1" color="text.primary">
      {value}
    </Typography>
  </ButtonBase>
);
