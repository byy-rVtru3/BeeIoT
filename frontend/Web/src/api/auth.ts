import { api } from '@/config/api';
import type { ApiResponse, LoginRequest, LoginResponse, LoginResponseData } from '@/types/authType';
import type { UserInfo } from '@/types/userType';

// POST /auth/login
export const signInUser = async (data: LoginRequest): Promise<LoginResponse> => {
  const response = await api.post<ApiResponse<LoginResponseData>>('/auth/login', data);
  return response.data;
};

// GET /auth/me
export const fetchSession = async (signal?: AbortSignal): Promise<ApiResponse<UserInfo>> => {
  const response = await api.get<ApiResponse<UserInfo>>('/auth/me', { signal });
  return response.data;
};

// DELETE /auth/logout
export const logoutUser = async (): Promise<ApiResponse<null>> => {
  const response = await api.delete<ApiResponse<null>>('/auth/logout');
  return response.data;
};
