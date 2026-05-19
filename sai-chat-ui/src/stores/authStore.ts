import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '@/types/document';
import { UserRole } from '@/types/document';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (username: string, role?: UserRole) => void;
  logout: () => void;
  updateRole: (role: UserRole) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,

      login: (username: string, role: UserRole = UserRole.USER) => {
        const token = `mock-satoken-${Date.now()}`;
        localStorage.setItem('satoken', token);
        const user: User = {
          userId: username,
          username,
          role,
          token,
        };
        set({ user, isAuthenticated: true });
      },

      logout: () => {
        localStorage.removeItem('satoken');
        set({ user: null, isAuthenticated: false });
      },

      updateRole: (role: UserRole) => {
        set((state) => ({
          user: state.user ? { ...state.user, role } : null,
        }));
      },
    }),
    {
      name: 'sai-auth',
      partialize: (state) => ({ user: state.user, isAuthenticated: state.isAuthenticated }),
    }
  )
);
