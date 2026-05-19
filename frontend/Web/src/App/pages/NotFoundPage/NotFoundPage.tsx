import { Box, Typography } from '@mui/material';

const NotFoundPage = () => {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h6">Not found</Typography>
      <Typography sx={{ mt: 1 }}>Page does not exist.</Typography>
    </Box>
  );
};

export default NotFoundPage;
