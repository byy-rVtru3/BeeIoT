type Revalidator = { revalidate: () => void };
let _revalidator: Revalidator | null = null;

export const setRevalidator = (r: Revalidator | null) => {
  _revalidator = r;
};

export const getRevalidator = () => _revalidator;
