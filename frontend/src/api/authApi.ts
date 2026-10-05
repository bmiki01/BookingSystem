import apiClient from './client';
import { AuthResponse, User } from '../types';

export const authApi = {
  register: async (payload: { email: string; password: string; fullName: string }): Promise<AuthResponse> => {
    const res = await apiClient.post<AuthResponse>('/auth/register', payload);
    return res.data;
  },

  login: async (payload: { email: string; password: string }): Promise<AuthResponse> => {
    const res = await apiClient.post<AuthResponse>('/auth/login', payload);
    return res.data;
  },

  getCurrentUser: async (): Promise<User> => {
    const res = await apiClient.get<User>('/auth/me');
    return res.data;
  },
};
