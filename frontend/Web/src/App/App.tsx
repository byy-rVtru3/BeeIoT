import { CssBaseline, ThemeProvider } from '@mui/material';
import { Outlet } from 'react-router-dom';

import beeTheme from './theme';

const App = () => {
  return (
    <ThemeProvider theme={beeTheme}>
      <CssBaseline />
      <Outlet />
    </ThemeProvider>
  );
};

export default App;
