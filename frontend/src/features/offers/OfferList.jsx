/** FR-05 - the public promotions page. Member: Hunaif A.A. */
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Tag, ArrowRight } from '@phosphor-icons/react';

import { offersApi } from './offers.api.js';
import { money, isoDate } from '../../api/client.js';
import { Alert, Empty } from '../../components/Ui.jsx';

export default function OfferList() {
  const [offers, setOffers] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    offersApi.listActive()
      .then((r) => setOffers(r.data))
      .catch((e) => { setError(e); setOffers([]); });
  }, []);

  return (
    <div className="page">
      <div className="shell">
        <div className="page-head">
          <h1>Offers running right now</h1>
          <p>Seasonal discounts published by our guides. Apply one when you book.</p>
        </div>

        <Alert error={error} />

        {offers === null ? (
          <div className="grid grid-3">
            {[0, 1, 2].map((i) => (
              <div className="card" key={i}>
                <div className="skeleton sk-line" style={{ width: '30%' }} />
                <div className="skeleton sk-line" style={{ width: '80%', marginTop: 14 }} />
                <div className="skeleton sk-line" style={{ width: '60%' }} />
              </div>
            ))}
          </div>
        ) : offers.length === 0 ? (
          <Empty
            title="No promotions at the moment"
            action={<Link to="/packages" className="btn">Browse tours</Link>}
          >
            Check back before the next travel season.
          </Empty>
        ) : (
          <div className="grid grid-3">
            {offers.map((o) => (
              <article className="card" key={o.offer_id}>
                <span className="badge badge-ACTIVE">
                  <Tag size={12} weight="fill" />
                  {o.discount_type === 'PERCENT'
                    ? `${Number(o.discount_value)}% off`
                    : `${money(o.discount_value)} off`}
                </span>

                <h3 style={{ marginTop: 12 }}>{o.title}</h3>
                <p className="muted">{o.description}</p>
                <p className="tiny">
                  {o.guide_name} · ends {isoDate(o.end_date)}
                </p>

                {o.packages?.length > 0 && (
                  <>
                    <hr className="divider" style={{ margin: '14px 0' }} />
                    <p className="tiny" style={{ marginBottom: 8 }}>Applies to</p>
                    <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                      {o.packages.map((p) => (
                        <li key={p.package_id} style={{ marginBottom: 6 }}>
                          <Link to={`/packages/${p.package_id}`} className="row" style={{ gap: 6, fontSize: '.9rem' }}>
                            {p.title} <ArrowRight size={13} />
                          </Link>
                        </li>
                      ))}
                    </ul>
                  </>
                )}
              </article>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
