/** SHARED FILE - site footer. */
import { Link } from 'react-router-dom';
import { DESTINATIONS } from '../lib/images.js';

export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="footer">
      <div className="shell">
        <div className="footer-grid">
          <div>
            <Link to="/" className="brand">Ceylon <span>Guides</span></Link>
            <p className="muted" style={{ marginTop: 10, maxWidth: '34ch' }}>
              Book a verified local guide for your journey through Sri Lanka. Every
              guide is checked by our team before their profile goes live.
            </p>
          </div>

          <div>
            <h4>Explore</h4>
            <ul>
              <li><Link to="/packages">All tours</Link></li>
              <li><Link to="/offers">Current offers</Link></li>
              <li><Link to="/register">Become a guide</Link></li>
            </ul>
          </div>

          <div>
            <h4>Destinations</h4>
            <ul>
              {DESTINATIONS.slice(0, 4).map((d) => (
                <li key={d.name}>
                  <Link to={`/packages?destination=${encodeURIComponent(d.query)}`}>{d.name}</Link>
                </li>
              ))}
            </ul>
          </div>

          <div>
            <h4>Account</h4>
            <ul>
              <li><Link to="/login">Log in</Link></li>
              <li><Link to="/register">Create an account</Link></li>
              <li><Link to="/my-bookings">My bookings</Link></li>
            </ul>
          </div>
        </div>

        <div className="footer-bottom">
          <span>{year} Ceylon Guides. A student project for SE2030 Software Engineering.</span>
          <span>Photography from Unsplash</span>
        </div>
      </div>
    </footer>
  );
}
