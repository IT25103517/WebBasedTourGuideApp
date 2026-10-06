/**
 * FR-03 Tour Itinerary Management - the guide's day plan editor.
 * Member: Wijesinghe W.M.P.N. (IT25103517)
 */
import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Plus, Trash } from '@phosphor-icons/react';

import { itinerariesApi } from './itineraries.api.js';
import { packagesApi } from '../packages/packages.api.js';
import ActivityForm from './ActivityForm.jsx';
import { Alert, Empty, Field } from '../../components/Ui.jsx';

export default function ManageItinerary() {
  const { packageId } = useParams();
  const [pkg, setPkg] = useState(null);
  const [days, setDays] = useState([]);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);
  const [newDay, setNewDay] = useState({ day_number: 1, day_title: '', description: '' });
  const [editingDay, setEditingDay] = useState(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const [p, d] = await Promise.all([
        packagesApi.getOne(packageId),
        itinerariesApi.listByPackage(packageId),
      ]);
      setPkg(p.data);
      setDays(d.data);
      setNewDay((n) => ({ ...n, day_number: d.data.length + 1 }));
    } catch (err) { setError(err); }
  }, [packageId]);

  useEffect(() => { load(); }, [load]);

  async function run(fn, message) {
    setError(null); setNotice(null);
    try { await fn(); setNotice(message); load(); }
    catch (err) { setError(err); }
  }

  function addDay(e) {
    e.preventDefault();
    run(() => itinerariesApi.createDay({
      package_id: Number(packageId),
      day_number: Number(newDay.day_number),
      day_title: newDay.day_title,
      description: newDay.description,
    }), 'Day added.');
    setNewDay({ day_number: days.length + 2, day_title: '', description: '' });
  }

  function saveDay(e) {
    e.preventDefault();
    run(() => itinerariesApi.updateDay(editingDay.itinerary_id, {
      day_number: Number(editingDay.day_number),
      day_title: editingDay.day_title,
      description: editingDay.description,
    }), 'Day updated.');
    setEditingDay(null);
  }

  return (
    <div className="page">
      <div className="shell">
        <div className="page-head">
          <p className="muted" style={{ marginBottom: 6 }}>
            <Link to="/guide/packages">Back to my tours</Link>
          </p>
          <h1>Itinerary{pkg ? `: ${pkg.title}` : ''}</h1>
          <p>{pkg ? `This tour runs for ${pkg.duration_days} ${pkg.duration_days === 1 ? 'day' : 'days'}.` : ''}</p>
        </div>

        <Alert error={error} success={notice} />

        <div className="card" style={{ marginBottom: 28 }}>
          <h3>Add a day</h3>
          <form onSubmit={addDay}>
            <div className="field-row">
              <Field label="Day number" htmlFor="d-num">
                <input
                  id="d-num" type="number" min={1} max={pkg?.duration_days || 60} required
                  value={newDay.day_number}
                  onChange={(e) => setNewDay({ ...newDay, day_number: e.target.value })}
                />
              </Field>
              <Field label="Day title" htmlFor="d-title">
                <input
                  id="d-title" required value={newDay.day_title}
                  placeholder="Arrival and Dambulla Cave Temple"
                  onChange={(e) => setNewDay({ ...newDay, day_title: e.target.value })}
                />
              </Field>
            </div>
            <Field label="Description" htmlFor="d-desc">
              <textarea
                id="d-desc" value={newDay.description}
                onChange={(e) => setNewDay({ ...newDay, description: e.target.value })}
              />
            </Field>
            <button type="submit" className="btn"><Plus size={16} weight="bold" /> Add day</button>
          </form>
        </div>

        {days.length === 0 ? (
          <Empty title="No days yet">Add day 1 above to start building the plan.</Empty>
        ) : (
          <div className="stack">
            {days.map((d) => (
              <div className="card" key={d.itinerary_id}>
                {editingDay?.itinerary_id === d.itinerary_id ? (
                  <form onSubmit={saveDay}>
                    <div className="field-row">
                      <Field label="Day number">
                        <input
                          type="number" min={1} value={editingDay.day_number}
                          onChange={(e) => setEditingDay({ ...editingDay, day_number: e.target.value })}
                        />
                      </Field>
                      <Field label="Day title">
                        <input
                          value={editingDay.day_title}
                          onChange={(e) => setEditingDay({ ...editingDay, day_title: e.target.value })}
                        />
                      </Field>
                    </div>
                    <Field label="Description">
                      <textarea
                        value={editingDay.description || ''}
                        onChange={(e) => setEditingDay({ ...editingDay, description: e.target.value })}
                      />
                    </Field>
                    <div className="card-actions">
                      <button className="btn btn-sm" type="submit">Save</button>
                      <button className="btn btn-ghost btn-sm" type="button" onClick={() => setEditingDay(null)}>
                        Cancel
                      </button>
                    </div>
                  </form>
                ) : (
                  <>
                    <div className="row-between">
                      <div>
                        <p className="tiny" style={{ marginBottom: 2 }}>Day {d.day_number}</p>
                        <h3 style={{ margin: 0 }}>{d.day_title}</h3>
                      </div>
                      <div className="row" style={{ gap: 6 }}>
                        <button className="btn btn-ghost btn-sm" onClick={() => setEditingDay({ ...d })}>Edit</button>
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => window.confirm('Delete this day and its activities?')
                            && run(() => itinerariesApi.deleteDay(d.itinerary_id), 'Day deleted.')}
                        >
                          Delete
                        </button>
                      </div>
                    </div>

                    {d.description && <p className="muted" style={{ marginTop: 10 }}>{d.description}</p>}

                    {d.activities?.length > 0 ? (
                      <ul className="activity-list">
                        {d.activities.map((a) => (
                          <li key={a.activity_id}>
                            <span className="activity-time">
                              {a.start_time ? `${a.start_time}${a.end_time ? ` - ${a.end_time}` : ''}` : 'Flexible'}
                            </span>
                            <span style={{ flex: 1 }}>
                              {a.activity_name}
                              {a.location && <span className="muted"> · {a.location}</span>}
                            </span>
                            <button
                              className="btn btn-quiet btn-sm"
                              aria-label={`Remove ${a.activity_name}`}
                              onClick={() => run(() => itinerariesApi.deleteActivity(a.activity_id), 'Activity removed.')}
                            >
                              <Trash size={15} />
                            </button>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="muted" style={{ marginTop: 12 }}>No activities on this day yet.</p>
                    )}

                    <ActivityForm
                      onAdd={(body) => run(() => itinerariesApi.addActivity(d.itinerary_id, body), 'Activity added.')}
                    />
                  </>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
