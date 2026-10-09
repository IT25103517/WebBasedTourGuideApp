/**
 * FR-02 Availability Calendar Management - the guide's calendar screen.
 * Member: Ashfak M.A.M. (IT25103511)
 */
import { useCallback, useEffect, useState } from 'react';
import { CaretLeft, CaretRight } from '@phosphor-icons/react';

import { availabilityApi } from './availability.api.js';
import { packagesApi } from '../packages/packages.api.js';
import CalendarGrid from './CalendarGrid.jsx';
import { isoDate, money } from '../../api/client.js';
import { Alert, Badge, Empty, Field } from '../../components/Ui.jsx';

const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'];

export default function ManageCalendar() {
  const today = new Date();
  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth() + 1);

  const [calendar, setCalendar] = useState(null);
  const [windows, setWindows] = useState([]);
  const [packages, setPackages] = useState([]);
  const [selected, setSelected] = useState([]);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);

  const [form, setForm] = useState({
    start_date: '', end_date: '', status: 'AVAILABLE',
    max_group_size: 10, price_override: '', package_id: '', note: '',
  });

  const load = useCallback(async () => {
    setError(null);
    try {
      const [cal, win, pkg] = await Promise.all([
        availabilityApi.myCalendar(year, month),
        availabilityApi.listMine(),
        packagesApi.listMine(),
      ]);
      setCalendar(cal.data); setWindows(win.data); setPackages(pkg.data);
    } catch (err) { setError(err); }
  }, [year, month]);

  useEffect(() => { load(); }, [load]);

  /** Clicking days fills the from and to boxes automatically. */
  function toggleDay(day) {
    setSelected((prev) => {
      const next = prev.includes(day.date)
        ? prev.filter((d) => d !== day.date)
        : [...prev, day.date].sort();
      if (next.length) setForm((f) => ({ ...f, start_date: next[0], end_date: next[next.length - 1] }));
      return next;
    });
  }

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function save(e) {
    e.preventDefault();
    setError(null); setNotice(null);
    try {
      await availabilityApi.create({
        start_date: form.start_date,
        end_date: form.end_date || form.start_date,
        status: form.status,
        max_group_size: Number(form.max_group_size) || 10,
        price_override: form.price_override === '' ? null : Number(form.price_override),
        package_id: form.package_id === '' ? null : Number(form.package_id),
        note: form.note,
      });
      setNotice(form.status === 'BLOCKED' ? 'Those dates are now blocked.' : 'Those dates are open for booking.');
      setSelected([]);
      setForm({ ...form, start_date: '', end_date: '', note: '', price_override: '' });
      load();
    } catch (err) { setError(err); }
  }

  async function removeWindow(w) {
    if (!window.confirm('Remove this schedule entry?')) return;
    setError(null); setNotice(null);
    try { setNotice((await availabilityApi.remove(w.availability_id)).message); load(); }
    catch (err) { setError(err); }
  }

  async function toggleWindowStatus(w) {
    setError(null); setNotice(null);
    try {
      await availabilityApi.update(w.availability_id, {
        status: w.status === 'AVAILABLE' ? 'BLOCKED' : 'AVAILABLE',
      });
      setNotice('Availability updated.'); load();
    } catch (err) { setError(err); }
  }

  function shiftMonth(delta) {
    let m = month + delta, y = year;
    if (m < 1) { m = 12; y -= 1; }
    if (m > 12) { m = 1; y += 1; }
    setMonth(m); setYear(y); setSelected([]);
  }

  return (
    <div className="page">
      <div className="shell">
        <div className="page-head">
          <h1>My calendar</h1>
          <p>Open the dates you can guide, and block the ones you cannot.</p>
        </div>

        <Alert error={error} success={notice} />
        {error?.details?.booked_dates && (
          <div className="alert alert-info">
            These dates already have a booking: {error.details.booked_dates.join(', ')}
          </div>
        )}

        <div className="book-grid">
          <div className="card">
            <div className="row-between" style={{ marginBottom: 14 }}>
              <button className="btn btn-ghost btn-sm" onClick={() => shiftMonth(-1)}>
                <CaretLeft size={14} weight="bold" /> Previous
              </button>
              <b style={{ fontFamily: 'var(--font-display)', fontSize: '1.1rem' }}>
                {MONTHS[month - 1]} {year}
              </b>
              <button className="btn btn-ghost btn-sm" onClick={() => shiftMonth(1)}>
                Next <CaretRight size={14} weight="bold" />
              </button>
            </div>

            {calendar
              ? <CalendarGrid days={calendar.days} selected={selected} onToggleDay={toggleDay} />
              : <div className="skeleton" style={{ height: 300 }} />}

            {selected.length > 0 && (
              <p className="muted" style={{ marginTop: 10, marginBottom: 0 }}>
                {selected.length} {selected.length === 1 ? 'day' : 'days'} selected:
                {' '}{selected[0]}{selected.length > 1 && ` to ${selected[selected.length - 1]}`}
              </p>
            )}
          </div>

          <div className="card">
            <h3>Set availability</h3>
            <p className="muted">Click days on the calendar, or type the range below.</p>

            <form onSubmit={save}>
              <div className="field-row">
                <Field label="From" htmlFor="a-from">
                  <input id="a-from" type="date" required value={form.start_date} onChange={set('start_date')} />
                </Field>
                <Field label="To" htmlFor="a-to">
                  <input id="a-to" type="date" value={form.end_date} onChange={set('end_date')} />
                </Field>
              </div>

              <Field label="Mark these dates as" htmlFor="a-status">
                <select id="a-status" value={form.status} onChange={set('status')}>
                  <option value="AVAILABLE">Available for booking</option>
                  <option value="BLOCKED">Blocked</option>
                </select>
              </Field>

              <Field label="Applies to" htmlFor="a-pkg">
                <select id="a-pkg" value={form.package_id} onChange={set('package_id')}>
                  <option value="">All of my tours</option>
                  {packages.map((p) => (
                    <option key={p.package_id} value={p.package_id}>{p.title}</option>
                  ))}
                </select>
              </Field>

              <div className="field-row">
                <Field label="Max group size" htmlFor="a-size">
                  <input id="a-size" type="number" min={1} max={100} value={form.max_group_size} onChange={set('max_group_size')} />
                </Field>
                <Field label="Special price" htmlFor="a-price" help="Optional">
                  <input id="a-price" type="number" min={0} step="0.01" value={form.price_override} onChange={set('price_override')} />
                </Field>
              </div>

              <Field label="Note" htmlFor="a-note">
                <input id="a-note" value={form.note} onChange={set('note')} placeholder="Peak season, personal leave" />
              </Field>

              <button type="submit" className="btn btn-block">Save availability</button>
            </form>
          </div>
        </div>

        <h2 style={{ marginTop: 44, marginBottom: 18, fontSize: '1.3rem' }}>My schedule entries</h2>

        {windows.length === 0 ? (
          <Empty title="Nothing scheduled yet">
            Open a date range above so travellers can request those days.
          </Empty>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>From</th><th>To</th><th>Status</th><th>Applies to</th>
                  <th>Group</th><th>Price</th><th>Note</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {windows.map((w) => (
                  <tr key={w.availability_id}>
                    <td style={{ whiteSpace: 'nowrap' }}>{isoDate(w.start_date)}</td>
                    <td style={{ whiteSpace: 'nowrap' }}>{isoDate(w.end_date)}</td>
                    <td><Badge value={w.status} /></td>
                    <td>{w.package_title || 'All tours'}</td>
                    <td>{w.max_group_size}</td>
                    <td style={{ whiteSpace: 'nowrap' }}>{w.price_override ? money(w.price_override) : '-'}</td>
                    <td className="muted">{w.note || '-'}</td>
                    <td>
                      <div className="row" style={{ gap: 6 }}>
                        <button className="btn btn-ghost btn-sm" onClick={() => toggleWindowStatus(w)}>
                          {w.status === 'AVAILABLE' ? 'Block' : 'Open'}
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => removeWindow(w)}>Remove</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
