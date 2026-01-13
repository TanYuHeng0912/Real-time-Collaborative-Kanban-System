import { create } from 'zustand';

interface AuthState {
  token: string | null;
  user: {
    username: string;
    email: string;
    fullName?: string;
  } | null;
  setAuth: (token: string, user: { username: string; email: string; fullName?: string }) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: localStorage.getItem('token'),
  user: null,
  setAuth: (token, user) => {
    localStorage.setItem('token', token);
    set({ token, user });
  },
  logout: () => {
    localStorage.removeItem('token');
    set({ token: null, user: null });
  },
}));
