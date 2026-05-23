import { Box, Typography } from '@mui/material';
import { isRouteErrorResponse, useRouteError } from 'react-router-dom';

const RouterErrorBoundary = () => {
  const error = useRouteError();
  let message = 'Unexpected error';

  if (isRouteErrorResponse(error)) {
    message = `${error.status} ${error.statusText}`;
  } else if (error instanceof Error) {
    message = error.message;
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h6">Router error</Typography>
      <Typography sx={{ mt: 1 }}>{message}</Typography>
    </Box>
  );
};

export default RouterErrorBoundary;
