type IconExpandProps = {
  open: boolean;
};

const IconExpand = ({ open }: IconExpandProps) => (
  <svg
    width="20"
    height="20"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2.4"
    strokeLinecap="round"
  >
    <line x1="5" y1="12" x2="19" y2="12" />
    {!open ? <line x1="12" y1="5" x2="12" y2="19" /> : null}
  </svg>
);

export default IconExpand;
