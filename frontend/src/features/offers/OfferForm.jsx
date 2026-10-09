/** FR-05 - create or edit an offer and pick the tours it covers. */
import { useState } from 'react';
import { Alert, Field } from '../../components/Ui.jsx';

const EMPTY = {
  title: '', description: '', discount_type: 'PERCENT', discount_value: 10,
  start_date: '', end_date: '', status: 'ACTIVE',
};

export default function OfferForm({ initial, packages, onSave, onCancel }) {
  const [form, setForm] = useState(initial ? {
    ...EMPTY, ...initial,
    start_date: String(initial.start_date).slice(0, 10),
    end_date: String(initial.end_date).slice(0, 10),
  } : EMPTY);
  const [selected, setSelected] = useState(
    initial?.packages ? initial.packages.map((p) => p.package_id) : []
  );
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });
  const fieldError = (k) => (error?.details && error.details[k]) || undefined;

  function togglePackage(id) {
    setSelected((prev) => prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]);
  }

  async function submit(e) {
    e.preventDefault();
    if (!selected.length) { setError(new Error('Select at least one tour to promote.')); return; }
    setBusy(true); setError(null);
    try {
      await onSave({
        title: form.title,
        description: form.description,
        discount_type: form.discount_type,
        discount_value: Number(form.discount_value),
        start_date: form.start_date,
        end_date: form.end_date,
        status: form.status,
        package_ids: selected,
      });
    } catch (err) { setError(err); } finally { setBusy(false); }
  }

  return (
    <div className="card" style={{ marginBottom: 28 }}>
      <h3>{initial ? 'Edit offer' : 'New offer'}</h3>
      <Alert error={error} />

      <form onSubmit={submit} noValidate>
        <Field label="Offer title" htmlFor="o-title" error={fieldError('title')}>
          <input id="o-title" required value={form.title} onChange={set('title')} placeholder="Monsoon season deal" />
        </Field>

        <Field label="Description" htmlFor="o-desc" error={fieldError('description')}>
          <textarea id="o-desc" value={form.description || ''} onChange={set('description')} />
        </Field>

        <div className="field-row">
          <Field label="Discount type" htmlFor="o-type">
            <select id="o-type" value={form.discount_type} onChange={set('discount_type')}>
              <option value="PERCENT">Percentage off</option>
              <option value="FIXED">Fixed amount off</option>
            </select>
          </Field>
          <Field
            label={form.discount_type === 'PERCENT' ? 'Percent off' : 'Amount off (LKR)'}
            htmlFor="o-value" error={fieldError('discount_value')}
          >
            <input
              id="o-value" type="number" min={0.01} step="0.01" required
              max={form.discount_type === 'PERCENT' ? 100 : undefined}
              value={form.discount_value} onChange={set('discount_value')}
            />
          </Field>
        </div>

        <div className="field-row">
          <Field label="Starts" htmlFor="o-start" error={fieldError('start_date')}>
            <input id="o-start" type="date" required value={form.start_date} onChange={set('start_date')} />
          </Field>
          <Field label="Ends" htmlFor="o-end" error={fieldError('end_date')}>
            <input id="o-end" type="date" required value={form.end_date} onChange={set('end_date')} />
          </Field>
        </div>

        <Field label="Status" htmlFor="o-status">
          <select id="o-status" value={form.status} onChange={set('status')}>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Hidden</option>
            <option value="EXPIRED">Expired</option>
          </select>
        </Field>

        <Field label="Which tours does it apply to?">
          {packages.length === 0 ? (
            <p className="muted mb-0">Create a tour first.</p>
          ) : (
            <div className="stack-sm" style={{ marginTop: 4 }}>
              {packages.map((p) => (
                <label
                  key={p.package_id}
                  className="row"
                  style={{ fontWeight: 400, gap: 10, fontSize: '.92rem', cursor: 'pointer' }}
                >
                  <input
                    type="checkbox"
                    checked={selected.includes(p.package_id)}
                    onChange={() => togglePackage(p.package_id)}
                  />
                  {p.title} <span className="muted">{p.destination}</span>
                </label>
              ))}
            </div>
          )}
        </Field>

        <div className="card-actions">
          <button type="submit" className="btn" disabled={busy}>{busy ? 'Saving' : 'Save offer'}</button>
          <button type="button" className="btn btn-ghost" onClick={onCancel}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
