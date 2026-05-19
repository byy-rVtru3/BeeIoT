export type ArrowDirection = 'up' | 'down';

type IconArrowProps = {
  dir: ArrowDirection;
};

const IconArrow = ({ dir }: IconArrowProps) => (
  <svg
    width="16"
    height="16"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    style={{ transform: dir === 'down' ? 'rotate(180deg)' : 'none' }}
  >
    <polyline points="18 15 12 9 6 15" />
  </svg>
);

export default IconArrow;
