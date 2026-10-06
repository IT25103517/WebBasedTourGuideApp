/**
 * SHARED FILE - light or dark mode.
 *
 * By default we follow the operating system setting. Once the visitor picks a
 * mode we remember it on this browser only.
 */
import { useEffect, useState } from 'react';

const KEY = 'tg_theme';

function readStored() {
  try { return localStorage.getItem(KEY); } catch (_e) { return null; }
}

function systemPrefersDark() {
  return typeof window !== 'undefined'
    && window.matchMedia('(prefers-color-scheme: dark)').matches;
}

export function useTheme() {
  const [theme, setTheme] = useState(() => readStored() || 'system');
  const [systemDark, setSystemDark] = useState(systemPrefersDark);

  // follow the operating system while the visitor has not chosen for themselves
  useEffect(() => {
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const onChange = (e) => setSystemDark(e.matches);
    mq.addEventListener('change', onChange);
    return () => mq.removeEventListener('change', onChange);
  }, []);

  useEffect(() => {
    const root = document.documentElement;
    if (theme === 'system') root.removeAttribute('data-theme');
    else root.setAttribute('data-theme', theme);
    try {
      if (theme === 'system') localStorage.removeItem(KEY);
      else localStorage.setItem(KEY, theme);
    } catch (_e) { /* private browsing, not important */ }
  }, [theme]);

  const isDark = theme === 'system' ? systemDark : theme === 'dark';

  return {
    theme,
    isDark,
    setTheme,
    toggle: () => setTheme(isDark ? 'light' : 'dark'),
  };
}
