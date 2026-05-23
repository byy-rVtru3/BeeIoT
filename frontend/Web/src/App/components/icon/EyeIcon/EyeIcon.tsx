type EyeIconProps = {
  off?: boolean;
};

const EyeIcon = ({ off }: EyeIconProps) => (
  <svg
    width="22"
    height="22"
    viewBox="0 0 24 24"
    fill="none"
    stroke="rgba(0,0,0,0.55)"
    strokeWidth="1.8"
    strokeLinecap="round"
    strokeLinejoin="round"
  >
    <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" />
    <circle cx="12" cy="12" r="3" />
    {off ? <line x1="3" y1="3" x2="21" y2="21" /> : null}
  </svg>
);

export default EyeIcon;
