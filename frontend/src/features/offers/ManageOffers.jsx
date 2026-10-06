/**
 * FR-05 Special Offers and Promotion Management - the guide's promotions screen.
 * Member: Hunaif A.A. (IT25103523)
 */
import { useEffect, useState } from 'react';
import { Plus, Broom } from '@phosphor-icons/react';

import { offersApi } from './offers.api.js';
import { packagesApi } from '../packages/packages.api.js';
import OfferForm from './OfferForm.jsx';
import { money, isoDate } from '../../api/client.js';
import { Alert, Badge, Empty } from '../../components/Ui.jsx';

export default function ManageOffers() {
  const [offers, setOffers] = useState([]);
  const [packages, setPackages] = useState([]);
  const [editing, setEditing] = useState(null);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    try {
      const [o, p] = await Promise.all([offersApi.listMine(), packagesApi.listMine()]);
      setOffers(o.data); setPackages(p.data);
    } catch (err) { setError(err); } finally { setLoading(false); }
  }
  useEffect(() => { load(); }, []);

  async function save(data) {
    if (editing === 'new') await offersApi.create(data);
    else await offersApi.update(editing.offer_id, data);
    setEditing(null); setNotice('Offer saved.'); load();
  }

  async function remove(o) {
    if (!window.confirm(`Delete the offer "${o.title}"?`)) return;
    setError(null);
    try { setNotice((await offersApi.remove(o.offer_id)).message); load(); }
    catch (err) { setError(err); }
  }

  async function expireOld() {
    setError(null);
    try { setNotice((await offersApi.expireOld()).message); load(); }
    catch (err) { setError(err); }
  }

  return (
    <div className="page">
      <div className="shell">
        <div className="row-between page-head">
          <div>
            <h1>Promotions</h1>
            <p>Discount your tours for a season or a date range.</p>
          </div>
          {!editing && (
            <div className="row">
              <button className="btn btn-ghost" onClick={expireOld}>
                <Broom size={16} /> Clear expired
              </button>
              <button className="btn" onClick={() => { setNotice(null); setEditing('new'); }}>
                <Plus size={16} weight="bold" /> New offer
              </button>
            </div>
          )}
        </div>

        <Alert error={error} success={notice} />

        {editing && (
          <OfferForm
            initial={editing === 'new' ? null : editing}
            packages={packages}
            onSave={save}
            onCancel={() => setEditing(null)}
          />
        )}

        {loading ? (
          <div className="skeleton" style={{ height: 200, borderRadius: 'var(--r-lg)' }} />
        ) : offers.length === 0 ? (
          <Empty
            title="No promotions yet"
            action={<button className="btn" onClick={() => setEditing('new')}>Create an offer</button>}
          >
            A seasonal discount is a good way to fill quiet weeks.
          </Empty>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Offer</th><th>Discount</th><th>Valid</th>
                  <th>Status</th><th>Live now</th><th>Tours</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {offers.map((o) => (
                  <tr key={o.offer_id}>
                    <td>
                      {o.title}
                      {o.description && <div className="tiny">{o.description}</div>}
                    </td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      {o.discount_type === 'PERCENT'
                        ? `${Number(o.discount_value)}%`
                        : money(o.discount_value)}
                    </td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      {isoDate(o.start_date)} to {isoDate(o.end_date)}
                    </td>
                    <td><Badge value={o.status} /></td>
                    <td>{o.is_live ? 'Yes' : 'No'}</td>
                    <td className="muted">{o.packages?.map((p) => p.title).join(', ') || '-'}</td>
                    <td>
                      <div className="row" style={{ gap: 6 }}>
                        <button className="btn btn-ghost btn-sm" onClick={() => { setNotice(null); setEditing(o); }}>
                          Edit
                        </button>
                        <button className="btn btn-danger btn-sm" onClick={() => remove(o)}>Delete</button>
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
