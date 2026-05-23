import axios from 'axios';

import { api } from '@/config/api';
import type { AppDescription, UpdateAppDescriptionRequest } from '@/types/appDescriptionType';
import type { ApiResponse } from '@/types/authType';

const handleNotFound = (error: unknown) => {
  if (axios.isAxiosError(error) && error.response?.status === 404) {
    return null;
  }
  throw error;
};

// GET /app-description
export const fetchAppDescription = async (signal?: AbortSignal): Promise<AppDescription | null> => {
  try {
    const response = await api.get<ApiResponse<AppDescription>>('/app-description', { signal });
    return response.data.data;
  } catch (error) {
    return handleNotFound(error);
  }
};

// GET /admin/app-description
export const fetchAdminAppDescription = async (
  signal?: AbortSignal
): Promise<AppDescription | null> => {
  try {
    const response = await api.get<ApiResponse<AppDescription>>('/admin/app-description', {
      signal,
    });
    return response.data.data;
  } catch (error) {
    return handleNotFound(error);
  }
};

// PUT /admin/app-description
export const updateAdminAppDescription = async (
  data: UpdateAppDescriptionRequest
): Promise<AppDescription> => {
  const response = await api.put<ApiResponse<AppDescription>>('/admin/app-description', data);
  return response.data.data;
};
