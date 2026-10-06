/**
 * SHARED FILE - one thin wrapper around fetch used by all six features.
 * It attaches the JWT, parses JSON and turns an API error into a real Error.
 */
const BASE = '/api';

function token() {
  return localStorage.getItem('tg_token');
}

async function request(method, path, body) {
  const res = await fetch(BASE + path, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token() ? { Authorization: `Bearer ${token()}` } : {}),
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });

  let payload = null;
  try { payload = await res.json(); } catch (_e) { /* empty body */ }

  if (!res.ok) {
    const err = new Error((payload && payload.message) || `Request failed (${res.status})`);
    err.status = res.status;
    err.details = payload && payload.details;
    throw err;
  }
  return payload;
}

export const api = {
  get:    (path)        => request('GET', path),
  post:   (path, body)  => request('POST', path, body),
  put:    (path, body)  => request('PUT', path, body),
  patch:  (path, body)  => request('PATCH', path, body),
  del:    (path)        => request('DELETE', path),
};

/** Formats a number as Sri Lankan rupees. */
export const money = (n) =>
  new Intl.NumberFormat('en-LK', { style: 'currency', currency: 'LKR', maximumFractionDigits: 0 })
    .format(Number(n || 0));

/** YYYY-MM-DD from anything date-like. */
export const isoDate = (d) => (d ? new Date(d).toISOString().slice(0, 10) : '');
