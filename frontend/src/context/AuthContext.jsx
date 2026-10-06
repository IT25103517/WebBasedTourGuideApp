/** SHARED FILE - keeps the logged in user and the JWT for the whole app. */
import { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../api/client.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const saved = localStorage.getItem('tg_user');
    if (saved && localStorage.getItem('tg_token')) {
      try { setUser(JSON.parse(saved)); } catch (_e) { /* ignore */ }
    }
    setLoading(false);
  }, []);

  function persist({ user: u, token }) {
    localStorage.setItem('tg_token', token);
    localStorage.setItem('tg_user', JSON.stringify(u));
    setUser(u);
  }

  async function login(email, password) {
    const res = await api.post('/auth/login', { email, password });
    persist(res.data);
    return res.data.user;
  }

  async function register(form) {
    const res = await api.post('/auth/register', form);
    persist(res.data);
    return res.data.user;
  }

  function logout() {
    localStorage.removeItem('tg_token');
    localStorage.removeItem('tg_user');
    setUser(null);
  }

  const value = {
    user, loading, login, register, logout,
    isGuide:   user?.role === 'GUIDE',
    isTourist: user?.role === 'TOURIST',
    isAdmin:   user?.role === 'ADMIN',
  };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
