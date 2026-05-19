import { create } from 'zustand';
import { persist } from 'zustand/middleware';

import type { UserInfo } from '@/types/userType';

const LEGACY_STORAGE_KEY = 'beeiot_admin_user';

const readLegacyUser = (): UserInfo | null => {
  if (typeof localStorage === 'undefined') return null;
  try {
    const raw = localStorage.getItem(LEGACY_STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as Partial<UserInfo>;
    return parsed?.email ? { email: String(parsed.email), name: parsed.name } : null;
  } catch {
    return null;
  }
};

type AuthState = {
  token: string | null;
  user: UserInfo | null;
  signIn: (user: UserInfo, token?: string | null) => void;
  signOut: () => void;
  setToken: (token: string | null) => void;
  setUser: (user: UserInfo | null) => void;
  logout: () => void;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      user: readLegacyUser(),
      signIn: (user, token = null) => set({ user, token }),
      signOut: () => set({ token: null, user: null }),
      setToken: (token) => set({ token }),
      setUser: (user) => set({ user }),
      logout: () => set({ token: null, user: null }),
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ token: state.token, user: state.user }),
    }
  )
);
