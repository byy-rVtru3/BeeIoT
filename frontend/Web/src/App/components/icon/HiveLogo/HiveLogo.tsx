import { Box } from '@mui/material';

type HiveLogoProps = {
  size?: number;
};

const HiveLogo = ({ size = 56 }: HiveLogoProps) => (
  <Box
    sx={{
      width: size,
      height: size,
      borderRadius: '50%',
      background: 'rgb(255,182,39)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      boxShadow: '0 6px 18px rgba(255,182,39,0.4)',
    }}
  >
    <svg width={size * 0.55} height={size * 0.55} viewBox="0 0 24 24" fill="none">
      <path
        d="M6 3 L18 3 L22 12 L18 21 L6 21 L2 12 Z"
        stroke="#1a1a1a"
        strokeWidth="2"
        strokeLinejoin="round"
        fill="rgba(255,255,255,0.25)"
      />
      <path
        d="M9 8 L15 8 M9 12 L15 12 M9 16 L15 16"
        stroke="#1a1a1a"
        strokeWidth="1.5"
        strokeLinecap="round"
      />
    </svg>
  </Box>
);

export default HiveLogo;
