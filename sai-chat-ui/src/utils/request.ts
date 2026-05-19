import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';

const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const instance: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 60000,
  withCredentials: true,
});

// Request interceptor: attach mock Sa-Token cookie
instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('satoken');
  if (token) {
    // Sa-Token expects cookie or header named 'satoken'
    document.cookie = `satoken=${token}; path=/`;
    if (config.headers) {
      config.headers['satoken'] = token;
    }
  }
  return config;
});

// Response interceptor: unwrap Result<T> envelope
instance.interceptors.response.use(
  (response) => {
    const payload = response.data;
    // If response has the standard Result<T> shape
    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code === '0' || payload.code === 0) {
        return response;
      }
      return Promise.reject(new Error(payload.message || 'API Error'));
    }
    return response;
  },
  (error: AxiosError) => {
    if (error.response) {
      const data = error.response.data as { message?: string; code?: string };
      return Promise.reject(new Error(data?.message || error.message));
    }
    if (error.request) {
      return Promise.reject(new Error('网络连接失败，请检查后端服务是否启动'));
    }
    return Promise.reject(error);
  }
);

export default instance;
