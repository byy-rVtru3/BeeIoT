import { createTheme, responsiveFontSizes } from '@mui/material';

export const colors = {
  primary: '#FCBB2A',
  primaryDark: '#FFB627',
  primaryLight: '#FCBB2A',
  primaryExtraLight: '#F4F4F4',

  bgPrimary: '#FFFFFF',
  bgSecondary: '#F4F4F4',
  bgTertiary: '#FFFFFF',

  error: '#AB0000',

  borderExtraLight: '#F4F4F4',
  borderLight: '#FCBB2A',
  borderMedium: '#141414',

  textPrimary: '#000000',
  textInvert: '#FFFFFF',
  textSecondary: '#141414',
  textTertiary: '#232323',
  textQuaternary: '#888888',
  textError: '#AB0000',

  indicatorWhite: '#FFFFFF',
  indicatorLight: '#F4F4F4',
  indicatorMedium: '#888888',
  indicatorNormal: '#141414',
  indicatorError: '#AB0000',
};

const baseTheme = createTheme({
  cssVariables: true,
  spacing: 4,
  palette: {
    primary: {
      main: colors.primary,
      dark: colors.primaryDark,
      light: colors.primaryLight,
      contrastText: colors.textInvert,
    },
    error: {
      main: colors.error,
    },
    background: {
      default: colors.bgPrimary,
      paper: colors.bgTertiary,
    },
    text: {
      primary: colors.textPrimary,
      secondary: colors.textSecondary,
      disabled: colors.textQuaternary,
    },
  },

  shape: { borderRadius: 16 },

  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',

    h1: { fontSize: 64, fontWeight: 900, lineHeight: 1.125, letterSpacing: '-0.2px' },
    h2: { fontSize: 48, fontWeight: 900, lineHeight: 1.1667, letterSpacing: '-0.2px' },
    h3: { fontSize: 36, fontWeight: 700, lineHeight: 1.2222, letterSpacing: '-0.1px' },
    h4: { fontSize: 32, fontWeight: 500, lineHeight: 1.25 },
    h5: { fontSize: 24, fontWeight: 600, lineHeight: 1.3333 },
    h6: { fontSize: 20, fontWeight: 500, lineHeight: 1.4 },

    subtitle1: { fontSize: 16, fontWeight: 500, lineHeight: 1.5 },
    subtitle2: { fontSize: 16, fontWeight: 500, lineHeight: 1.5 },

    body1: { fontSize: 20, fontWeight: 400, lineHeight: 1.5 },
    body2: { fontSize: 16, fontWeight: 400, lineHeight: 1.5 },
    caption: { fontSize: 12, fontWeight: 400, lineHeight: 1.5 },

    button: { fontSize: 16, fontWeight: 600, lineHeight: 1.5, textTransform: 'none' },
    overline: { fontSize: 12, fontWeight: 400, lineHeight: 1.3333 },
  },

  components: {
    MuiButton: {
      defaultProps: {
        disableElevation: true,
        disableFocusRipple: true,
        disableRipple: false,
      },
      styleOverrides: {
        root: {
          borderRadius: 16,
          paddingInline: 48,
          paddingBlock: 12,
          textTransform: 'none',
        },
        contained: {
          color: colors.textPrimary,
          backgroundColor: colors.primary,
          '&.Mui-disabled': {
            backgroundColor: colors.primaryExtraLight,
            color: colors.textPrimary,
          },
          '&.MuiButton-loading': { backgroundColor: colors.primaryExtraLight },
          '&.Mui-focusVisible': { boxShadow: `0 0 0 2px ${colors.primaryExtraLight}` },
          '&:hover': { backgroundColor: colors.primaryDark },
        },
        outlined: {
          borderColor: colors.primary,
          color: colors.textPrimary,
          '&:hover': { borderColor: colors.primaryDark },
        },
        text: {
          color: colors.textPrimary,
          backgroundColor: 'transparent',
          '&.Mui-disabled': { color: colors.textQuaternary },
          '&:hover': { backgroundColor: colors.bgSecondary },
        },
      },
    },
    MuiInputLabel: {
      styleOverrides: {
        root: {
          fontSize: 14,
          fontWeight: 400,
          lineHeight: 1.4,
          transform: 'none',
          color: colors.textSecondary,
          '&.Mui-focused': { color: colors.textSecondary },
          '&.Mui-error': { color: colors.textSecondary },
          '&.Mui-disabled': { color: colors.textQuaternary },
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          borderWidth: 1,
          backgroundColor: colors.bgPrimary,
          fontSize: 16,
          fontWeight: 400,
          lineHeight: 1.5,
          '& input::placeholder, & textarea::placeholder': {
            color: colors.textTertiary,
            opacity: 1,
          },
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: colors.borderLight,
          },
          '&.Mui-disabled': {
            backgroundColor: colors.bgSecondary,
          },
          '&.Mui-disabled .MuiOutlinedInput-notchedOutline': {
            borderColor: colors.borderLight,
          },
          '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
            borderColor: colors.primary,
            borderWidth: 2,
          },
          '&.Mui-error .MuiOutlinedInput-notchedOutline': {
            borderColor: colors.error,
            borderWidth: 2,
          },
          '&:not(.Mui-focused):not(.Mui-error):not(.Mui-disabled):hover .MuiOutlinedInput-notchedOutline':
            {
              borderColor: colors.borderMedium,
            },
        },
        input: {
          padding: '12px 12px',
          '&:disabled': {
            WebkitTextFillColor: colors.textTertiary,
          },
        },
      },
    },
    MuiFormHelperText: {
      styleOverrides: {
        root: {
          fontSize: 12,
          fontWeight: 400,
          lineHeight: 1.5,
          color: colors.textTertiary,
          '&.Mui-error': {
            color: colors.error,
          },
        },
      },
    },
  },
});

export const theme = responsiveFontSizes(baseTheme);

export default theme;
