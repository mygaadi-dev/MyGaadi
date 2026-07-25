import { createContext, useContext, useMemo, useState } from 'react';
import { toast } from 'react-toastify';
import api, { unwrap } from '../api/api';

const AuthContext = createContext(null);

function persistAuth(data) {
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  localStorage.setItem('user', JSON.stringify(data.user));
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : null;
  });

  const completeLogin = (data, message = 'Welcome back!') => {
    persistAuth(data);
    setUser(data.user);
    toast.success(message);
    return data.user;
  };

  const login = async (email, password) => {
    const data = await api.post('/auth/login', { email, password, deviceInfo: navigator.userAgent }).then(unwrap);
    return completeLogin(data);
  };

  const startSellerLogin = async (email, password) => {
    const data = await api.post('/auth/seller/login/start', { email, password, deviceInfo: navigator.userAgent }).then(unwrap);
    toast.success(data?.message || 'OTP sent');
    return data;
  };

  const verifySellerLogin = async (email, otp) => {
    const data = await api.post('/auth/seller/login/verify', { email, otp, deviceInfo: navigator.userAgent }).then(unwrap);
    return completeLogin(data, 'Seller login verified');
  };

  const startAdminLogin = async (email, password) => {
    const data = await api.post('/auth/admin/login/start', { email, password, deviceInfo: navigator.userAgent }).then(unwrap);
    toast.success(data?.message || 'Admin OTP sent');
    return data;
  };

  const verifyAdminLogin = async (email, otp) => {
    const data = await api.post('/auth/admin/login/verify', { email, otp, deviceInfo: navigator.userAgent }).then(unwrap);
    return completeLogin(data, 'Admin login verified');
  };

  const register = async (values) => {
    const data = await api.post('/auth/register', values).then(unwrap);
    return completeLogin(data, 'Account created');
  };

  const logout = () => {
    localStorage.clear();
    setUser(null);
    toast.info('Logged out');
  };

  const value = useMemo(() => ({
    user,
    setUser,
    login,
    startSellerLogin,
    verifySellerLogin,
    startAdminLogin,
    verifyAdminLogin,
    register,
    logout,
    isLoggedIn: !!user
  }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
