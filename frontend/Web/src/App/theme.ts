import { createTheme } from '@mui/material';

const beeTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: 'rgb(255,182,39)',
      light: 'rgb(255,201,113)',
      dark: 'rgb(228,147,19)',
      contrastText: '#000',
    },
    secondary: {
      main: 'rgb(35,35,35)',
    },
    background: {
      default: 'rgb(244,244,244)',
      paper: '#ffffff',
    },
    text: {
      primary: 'rgb(0,0,0)',
      secondary: 'rgba(0,0,0,0.55)',
    },
    divider: 'rgba(0,0,0,0.08)',
    error: {
      main: 'rgb(192,0,0)',
    },
  },
  typography: {
    fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
    h1: { fontWeight: 700 },
    h2: { fontWeight: 700 },
    h3: { fontWeight: 700 },
    h4: { fontWeight: 700 },
    h5: { fontWeight: 600 },
    h6: { fontWeight: 600 },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          paddingTop: 10,
          paddingBottom: 10,
          fontWeight: 600,
          boxShadow: 'none',
        },
        contained: {
          color: '#000',
          '&:hover': {
            backgroundColor: 'rgb(228,147,19)',
            boxShadow: 'none',
          },
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          backgroundColor: '#fff',
          '& fieldset': {
            borderColor: 'rgb(255,182,39)',
            borderWidth: 1,
          },
          '&:hover fieldset': {
            borderColor: 'rgb(228,147,19) !important',
          },
          '&.Mui-focused fieldset': {
            borderColor: 'rgb(255,182,39) !important',
            borderWidth: 2,
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        rounded: {
          borderRadius: 16,
        },
      },
    },
  },
});

export default beeTheme;
