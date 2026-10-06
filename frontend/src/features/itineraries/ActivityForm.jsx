/** FR-03 - add one activity to a day. Member: Wijesinghe W.M.P.N. */
import { useState } from 'react';
import { Plus } from '@phosphor-icons/react';
import { Field } from '../../components/Ui.jsx';

export default function ActivityForm({ onAdd }) {
  const [form, setForm] = useState({ activity_name: '', location: '', start_time: '', end_time: '' });
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    try {
      await onAdd({
        activity_name: form.activity_name,
        location: form.location || null,
        start_time: form.start_time || null,
        end_time: form.end_time || null,
      });
      setForm({ activity_name: '', location: '', start_time: '', end_time: '' });
    } finally { setBusy(false); }
  }

  return (
    <form onSubmit={submit} className="activity-form">
      <Field label="Activity">
        <input required value={form.activity_name} onChange={set('activity_name')} placeholder="Sigiriya rock climb" />
      </Field>
      <Field label="Location">
        <input value={form.location} onChange={set('location')} placeholder="Sigiriya" />
      </Field>
      <Field label="Start">
        <input type="time" value={form.start_time} onChange={set('start_time')} />
      </Field>
      <Field label="End">
        <input type="time" value={form.end_time} onChange={set('end_time')} />
      </Field>
      <button className="btn btn-sm" type="submit" disabled={busy}>
        <Plus size={14} weight="bold" /> Add
      </button>
    </form>
  );
}
