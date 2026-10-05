import React, { createContext, useContext, useEffect, useState } from 'react';
import { authApi } from '../api/authApi';
import { AuthResponse, User } from '../types';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  login: (email: string, pass: string) => Promise<AuthResponse>;
  register: (email: string, pass: string, fullName: string) => Promise<AuthResponse>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const initAuth = async () => {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        try {
          const currentUser = await authApi.getCurrentUser();
          setUser(currentUser);
          setToken(storedToken);
        } catch {
          localStorage.removeItem('token');
          setToken(null);
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, pass: string): Promise<AuthResponse> => {
    const res = await authApi.login({ email, password: pass });
    localStorage.setItem('token', res.accessToken);
    setToken(res.accessToken);
    setUser(res.user);
    return res;
  };

  const register = async (email: string, pass: string, fullName: string): Promise<AuthResponse> => {
    const res = await authApi.register({ email, password: pass, fullName });
    localStorage.setItem('token', res.accessToken);
    setToken(res.accessToken);
    setUser(res.user);
    return res;
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
    isLoading,
    login,
    register,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
