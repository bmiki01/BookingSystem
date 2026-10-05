import axios from 'axios';
import { ApiError } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.data) {
      const apiError = error.response.data as ApiError;
      return Promise.reject(apiError);
    }
    return Promise.reject({
      timestamp: new Date().toISOString(),
      status: error.response?.status || 500,
      errorCode: 'NETWORK_ERROR',
      message: error.message || 'Network connection failed',
      path: '',
    } as ApiError);
  }
);

export default apiClient;
