/** SHARED FILE - the small presentational pieces every feature reuses. */
import { Star, StarHalf } from '@phosphor-icons/react';

export function Alert({ error, success, info }) {
  if (error) {
    return (
      <div className="alert alert-error" role="alert">
        {String(error.message || error)}
        {error.details && typeof error.details === 'object' && (
          <ul>
            {Object.entries(error.details).map(([k, v]) => (
              <li key={k}>{Array.isArray(v) ? v.join(', ') : String(v)}</li>
            ))}
          </ul>
        )}
      </div>
    );
  }
  if (success) return <div className="alert alert-success" role="status">{success}</div>;
  if (info) return <div className="alert alert-info">{info}</div>;
  return null;
}

export function Badge({ value }) {
  if (!value) return null;
  return <span className={`badge badge-${value}`}>{String(value).toLowerCase()}</span>;
}

/** Read-only star rating. */
export function Stars({ value, size = 15 }) {
  const v = Number(value) || 0;
  return (
    <span className="stars" title={`${v.toFixed(1)} out of 5`} aria-label={`Rated ${v.toFixed(1)} out of 5`}>
      {[1, 2, 3, 4, 5].map((n) =>
        v >= n ? (
          <Star key={n} size={size} weight="fill" />
        ) : v >= n - 0.5 ? (
          <StarHalf key={n} size={size} weight="fill" />
        ) : (
          <Star key={n} size={size} weight="regular" />
        )
      )}
    </span>
  );
}

/** Star rating plus the number, used in listings. */
export function Rating({ value, count }) {
  if (!value) return <span className="muted">No reviews yet</span>;
  return (
    <span className="rating-inline">
      <Stars value={value} size={14} />
      <b>{Number(value).toFixed(1)}</b>
      {count != null && <span className="muted">({count})</span>}
    </span>
  );
}

export function Empty({ title, children, action }) {
  return (
    <div className="empty">
      {title && <h3>{title}</h3>}
      {children && <p className="mb-0">{children}</p>}
      {action && <div style={{ marginTop: 18 }}>{action}</div>}
    </div>
  );
}

/** Label above, control, then optional help or error below. */
export function Field({ label, help, error, htmlFor, children }) {
  return (
    <div className="field">
      {label && <label htmlFor={htmlFor}>{label}</label>}
      {children}
      {error ? <span className="field-error">{error}</span>
        : help ? <span className="field-help">{help}</span> : null}
    </div>
  );
}

/** Placeholder cards shown while a listing loads. */
export function TileSkeletons({ count = 6 }) {
  return (
    <div className="grid grid-3">
      {Array.from({ length: count }, (_, i) => (
        <div className="skeleton-tile" key={i}>
          <div className="skeleton sk-media" />
          <div className="sk-body">
            <div className="skeleton sk-line" style={{ width: '75%' }} />
            <div className="skeleton sk-line" style={{ width: '45%' }} />
            <div className="skeleton sk-line" style={{ width: '60%', marginTop: 18 }} />
          </div>
        </div>
      ))}
    </div>
  );
}

/** Initials avatar, used beside reviews. */
export function Avatar({ name }) {
  const initials = String(name || '?')
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0].toUpperCase())
    .join('');
  return <span className="avatar" aria-hidden="true">{initials}</span>;
}
