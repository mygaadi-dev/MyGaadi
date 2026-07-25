import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem('accessToken');
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

let refreshing = false;
let pending = [];

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config || {};
    const refreshToken = localStorage.getItem('refreshToken');
    if (error.response?.status === 401 && refreshToken && !original._retry) {
      original._retry = true;
      if (!refreshing) {
        refreshing = true;
        try {
          const res = await axios.post('http://localhost:8080/api/auth/refresh', { refreshToken });
          const data = unwrap(res);
          localStorage.setItem('accessToken', data.accessToken);
          localStorage.setItem('refreshToken', data.refreshToken);
          localStorage.setItem('user', JSON.stringify(data.user));
          pending.forEach((cb) => cb(data.accessToken));
          pending = [];
        } catch (e) {
          localStorage.clear();
          window.location.href = '/login';
          return Promise.reject(e);
        } finally {
          refreshing = false;
        }
      }
      return new Promise((resolve) => {
        pending.push((token) => {
          original.headers = original.headers || {};
          original.headers.Authorization = `Bearer ${token}`;
          resolve(api(original));
        });
      });
    }
    return Promise.reject(error);
  }
);

export default api;

export const unwrap = (res) => {
  const payload = res?.data;
  if (!payload) return payload;
  // Spring ApiResponse format: { success, message, data }
  if (Object.prototype.hasOwnProperty.call(payload, 'data') && !payload.code) return payload.data;
  // Direct MS .NET KYC format: { success, code, message, data }
  return payload;
};
