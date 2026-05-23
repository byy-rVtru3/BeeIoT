export type NavIconName = 'description' | 'instruction' | 'logout' | 'logo';

type NavIconProps = {
  name: NavIconName;
};

const NavIcon = ({ name }: NavIconProps) => {
  const stroke = 'currentColor';
  const sw = 1.8;
  const common = {
    width: 22,
    height: 22,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke,
    strokeWidth: sw,
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
  };

  switch (name) {
    case 'description':
      return (
        <svg {...common}>
          <path d="M14 3H7a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8z" />
          <path d="M14 3v5h5" />
          <path d="M9 13h6M9 17h4" />
        </svg>
      );
    case 'instruction':
      return (
        <svg {...common}>
          <rect x="4" y="4" width="16" height="16" rx="2" />
          <path d="M8 9h8M8 13h8M8 17h5" />
        </svg>
      );
    case 'logout':
      return (
        <svg {...common}>
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <path d="M16 17l5-5-5-5" />
          <path d="M21 12H9" />
        </svg>
      );
    case 'logo':
      return (
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <path
            d="M6 3 L18 3 L22 12 L18 21 L6 21 L2 12 Z"
            stroke="#1a1a1a"
            strokeWidth="2"
            strokeLinejoin="round"
            fill="rgb(255,182,39)"
          />
        </svg>
      );
    default:
      return null;
  }
};

export default NavIcon;
