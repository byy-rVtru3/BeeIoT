import { Box } from '@mui/material';

type IconBulletProps = {
  n: number;
};

const IconBullet = ({ n }: IconBulletProps) => (
  <Box
    sx={{
      width: 28,
      height: 28,
      borderRadius: '50%',
      border: '1.5px solid rgb(255,182,39)',
      bgcolor: 'rgba(255,182,39,0.15)',
      color: '#000',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      fontWeight: 700,
      fontSize: 13,
      flexShrink: 0,
    }}
  >
    {n}
  </Box>
);

export default IconBullet;
