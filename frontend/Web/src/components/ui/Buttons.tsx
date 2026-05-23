import {
  Button,
  ButtonBase,
  Fab,
  type ButtonProps,
  type FabProps,
  Typography,
} from '@mui/material';
import type { ReactNode } from 'react';

type PrimaryButtonProps = ButtonProps;

type SettingsButtonProps = ButtonProps & {
  exit?: boolean;
};

type LabelButtonProps = {
  label: string;
  onClick: () => void;
  disabled?: boolean;
};

type FloatingActionButtonProps = Omit<FabProps, 'children'> & {
  icon: ReactNode;
};

export const PrimaryButton = ({ sx, fullWidth = true, ...props }: PrimaryButtonProps) => (
  <Button
    variant="outlined"
    fullWidth={fullWidth}
    {...props}
    sx={{
      borderColor: 'primary.main',
      borderWidth: 2,
      boxSizing: 'border-box',
      backgroundColor: 'background.paper',
      color: 'text.primary',
      paddingY: 2,
      paddingX: 2,
      transition: 'transform 100ms ease, border-color 100ms ease, background-color 100ms ease',
      '&:hover': {
        borderColor: 'primary.dark',
        backgroundColor: 'background.default',
      },
      '&:active': {
        transform: 'scale(0.95)',
      },
      '&.Mui-disabled': {
        opacity: 0.5,
      },
      ...sx,
    }}
  />
);

export const SettingsButton = ({
  sx,
  exit = false,
  fullWidth = true,
  ...props
}: SettingsButtonProps) => (
  <Button
    variant="outlined"
    fullWidth={fullWidth}
    {...props}
    sx={{
      justifyContent: 'flex-start',
      textAlign: 'left',
      borderColor: exit ? 'error.main' : 'primary.main',
      borderWidth: 2,
      boxSizing: 'border-box',
      backgroundColor: 'background.paper',
      color: 'text.primary',
      paddingY: 2,
      paddingX: 2,
      transition: 'transform 100ms ease, border-color 100ms ease, background-color 100ms ease',
      '&:hover': {
        borderColor: exit ? 'error.main' : 'primary.dark',
        backgroundColor: 'background.default',
      },
      '&:active': {
        transform: 'scale(0.95)',
      },
      '& .MuiButton-endIcon': {
        marginLeft: 'auto',
      },
      ...sx,
    }}
  />
);

export const LabelButton = ({ label, onClick, disabled = false }: LabelButtonProps) => (
  <ButtonBase
    onClick={onClick}
    disabled={disabled}
    sx={{
      display: 'inline-flex',
      alignItems: 'center',
      color: 'text.primary',
      transition: 'transform 100ms ease, opacity 100ms ease',
      '&:active': {
        transform: disabled ? 'none' : 'scale(0.92)',
        opacity: disabled ? 0.4 : 0.6,
      },
      '&.Mui-disabled': {
        opacity: 0.4,
      },
    }}
  >
    <Typography variant="body2" color="text.primary">
      {label}
    </Typography>
  </ButtonBase>
);

export const FloatingActionButton = ({ icon, sx, ...props }: FloatingActionButtonProps) => (
  <Fab
    color="default"
    {...props}
    sx={{
      width: 56,
      height: 56,
      minHeight: 56,
      border: '2px solid',
      borderColor: 'primary.main',
      backgroundColor: 'background.paper',
      color: 'text.primary',
      boxShadow: '0 4px 16px rgba(0, 0, 0, 0.12)',
      transition: 'transform 100ms ease, border-color 100ms ease',
      '&:hover': {
        borderColor: 'primary.dark',
      },
      '&:active': {
        transform: 'scale(0.95)',
      },
      ...sx,
    }}
  >
    {icon}
  </Fab>
);
