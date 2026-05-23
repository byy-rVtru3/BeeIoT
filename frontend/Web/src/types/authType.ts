export type ApiResponse<T> = {
  status: string;
  message: string;
  data: T;
};

export type LoginRequest = {
  email: string;
  password: string;
};

export type LoginResponseData = {
  token: string;
};

export type LoginResponse = ApiResponse<LoginResponseData>;
