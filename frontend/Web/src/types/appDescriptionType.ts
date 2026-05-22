export type AppDescription = {
  title: string;
  short: string;
  full: string;
  updated_at?: string;
  updated_by?: string;
};

export type UpdateAppDescriptionRequest = {
  title: string;
  short: string;
  full: string;
};
