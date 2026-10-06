import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { Alert, Field } from '../components/Ui.jsx';
import SmartImage from '../components/SmartImage.jsx';
import { DESTINATIONS, unsplashUrl, fallbackUrl } from '../lib/images.js';

const EMPTY = {
  full_name: '', email: '', password: '', phone: '', role: 'TOURIST',
  nationality: '', preferred_language: '',
  experience_years: '', languages: '', base_location: '',
};

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const fieldError = (k) => (error?.details && error.details[k]) || undefined;
  const isGuide = form.role === 'GUIDE';

  async function submit(e) {
    e.preventDefault();
    setBusy(true); setError(null);
    try {
      const payload = { ...form };
      if (isGuide) { delete payload.nationality; delete payload.preferred_language; }
      else { delete payload.experience_years; delete payload.languages; delete payload.base_location; }
      await register(payload);
      navigate(isGuide ? '/guide/packages' : '/packages');
    } catch (err) { setError(err); } finally { setBusy(false); }
  }

  const art = DESTINATIONS[1]; // Ella

  return (
    <div className="auth">
      <div className="auth-art">
        <SmartImage
          eager
          src={unsplashUrl(art.photo, 1400, 1600)}
          fallback={fallbackUrl(art.seed, 1400, 1600)}
          alt="The Nine Arch Bridge curving through jungle near Ella"
        />
        <div className="auth-art-text">
          <h2>{isGuide ? 'Show travellers what you know' : 'Two hundred tours, one island'}</h2>
          <p>
            {isGuide
              ? 'Publish your tours, set your own calendar and take bookings directly.'
              : 'Create an account to book a guide and keep all your trip details in one place.'}
          </p>
        </div>
      </div>

      <div className="auth-form">
        <div className="auth-form-inner">
          <h1 style={{ fontSize: '2rem' }}>Create your account</h1>
          <p className="muted" style={{ marginBottom: 26 }}>It takes about a minute.</p>

          <Alert error={error} />

          <form onSubmit={submit} noValidate>
            <Field label="I am joining as" htmlFor="role">
              <select id="role" value={form.role} onChange={set('role')}>
                <option value="TOURIST">A traveller looking for a guide</option>
                <option value="GUIDE">A tour guide offering tours</option>
              </select>
            </Field>

            <Field label="Full name" htmlFor="full_name" error={fieldError('full_name')}>
              <input id="full_name" required autoComplete="name" value={form.full_name} onChange={set('full_name')} />
            </Field>

            <Field label="Email address" htmlFor="email" error={fieldError('email')}>
              <input id="email" type="email" required autoComplete="email" value={form.email} onChange={set('email')} />
            </Field>

            <div className="field-row">
              <Field
                label="Password" htmlFor="password"
                help="At least 8 characters" error={fieldError('password')}
              >
                <input
                  id="password" type="password" required minLength={8} autoComplete="new-password"
                  value={form.password} onChange={set('password')}
                />
              </Field>
              <Field label="Phone" htmlFor="phone" error={fieldError('phone')}>
                <input id="phone" autoComplete="tel" value={form.phone} onChange={set('phone')} />
              </Field>
            </div>

            {isGuide ? (
              <>
                <div className="field-row">
                  <Field label="Years of experience" htmlFor="exp" error={fieldError('experience_years')}>
                    <input id="exp" type="number" min={0} max={70} value={form.experience_years} onChange={set('experience_years')} />
                  </Field>
                  <Field label="Based in" htmlFor="base" error={fieldError('base_location')}>
                    <input id="base" placeholder="Kandy" value={form.base_location} onChange={set('base_location')} />
                  </Field>
                </div>
                <Field label="Languages you speak" htmlFor="langs" error={fieldError('languages')}>
                  <input id="langs" placeholder="English, Sinhala, Tamil" value={form.languages} onChange={set('languages')} />
                </Field>
              </>
            ) : (
              <div className="field-row">
                <Field label="Nationality" htmlFor="nat" error={fieldError('nationality')}>
                  <input id="nat" placeholder="United Kingdom" value={form.nationality} onChange={set('nationality')} />
                </Field>
                <Field label="Preferred language" htmlFor="lang" error={fieldError('preferred_language')}>
                  <input id="lang" placeholder="English" value={form.preferred_language} onChange={set('preferred_language')} />
                </Field>
              </div>
            )}

            <button type="submit" className="btn btn-block btn-lg" disabled={busy}>
              {busy ? 'Creating your account' : 'Create account'}
            </button>
          </form>

          <p className="muted" style={{ marginTop: 20 }}>
            Already registered? <Link to="/login">Log in</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
