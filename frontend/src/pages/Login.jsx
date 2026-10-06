import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { Alert, Field } from '../components/Ui.jsx';
import SmartImage from '../components/SmartImage.jsx';
import { AUTH_IMAGE, unsplashUrl, fallbackUrl } from '../lib/images.js';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const next = location.state?.from || '/';

  async function submit(e) {
    e.preventDefault();
    setBusy(true); setError(null);
    try {
      await login(form.email, form.password);
      navigate(next);
    } catch (err) { setError(err); } finally { setBusy(false); }
  }

  function useDemo(email) {
    setForm({ email, password: 'Password@123' });
    setError(null);
  }

  return (
    <div className="auth">
      <div className="auth-art">
        <SmartImage
          eager
          src={unsplashUrl(AUTH_IMAGE, 1400, 1600)}
          fallback={fallbackUrl('sri-lanka-tea', 1400, 1600)}
          alt="Tea pickers working on a hillside estate in the Sri Lankan highlands"
        />
        <div className="auth-art-text">
          <h2>Your guide is already packed</h2>
          <p>Sign in to see your bookings, message your guide and leave a review after the tour.</p>
        </div>
      </div>

      <div className="auth-form">
        <div className="auth-form-inner">
          <h1 style={{ fontSize: '2rem' }}>Welcome back</h1>
          <p className="muted" style={{ marginBottom: 26 }}>Log in to continue planning your trip.</p>

          <Alert error={error} />

          <form onSubmit={submit} noValidate>
            <Field label="Email address" htmlFor="email">
              <input
                id="email" type="email" required autoComplete="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </Field>

            <Field label="Password" htmlFor="password">
              <input
                id="password" type="password" required autoComplete="current-password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
              />
            </Field>

            <button type="submit" className="btn btn-block btn-lg" disabled={busy}>
              {busy ? 'Signing in' : 'Log in'}
            </button>
          </form>

          <p className="muted" style={{ marginTop: 20 }}>
            New here? <Link to="/register">Create an account</Link>
          </p>

          <hr className="divider" style={{ margin: '26px 0' }} />

          <p className="tiny" style={{ marginBottom: 10 }}>Demo accounts for the evaluation</p>
          <div className="row">
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => useDemo('emma.tourist@mail.com')}>
              Tourist
            </button>
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => useDemo('kasun.guide@mail.com')}>
              Tour guide
            </button>
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => useDemo('admin@tourguide.lk')}>
              Administrator
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
