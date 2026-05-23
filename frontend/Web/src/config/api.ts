import axios from 'axios';

import { getRevalidator } from '@/config/revalidator';
import { useAuthStore } from '@/store/useAuthStore';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  timeout: 5000,
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  console.log(`[api] --> ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
  return config;
});

api.interceptors.response.use(
  (response) => {
    console.log(`[api] <-- ${response.status} ${response.config.url}`, response.data);
    return response;
  },
  (error) => {
    if (error.response) {
      console.error(`[api] ERR ${error.response.status} ${error.config?.url}`, error.response.data);
    } else {
      console.error(`[api] ERR ${error.config?.url}:`, error.message, error.code);
    }
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      getRevalidator()?.revalidate();
    }
    return Promise.reject(error);
  }
);
