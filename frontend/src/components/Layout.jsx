/** SHARED FILE - the site shell: navigation, page content and footer. */
import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { List, X, Sun, Moon } from '@phosphor-icons/react';
import { useAuth } from '../context/AuthContext.jsx';
import { useTheme } from '../lib/useTheme.js';
import Footer from './Footer.jsx';

export default function Layout({ children }) {
  const { user, logout, isGuide, isTourist, isAdmin } = useAuth();
  const { isDark, toggle } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const [open, setOpen] = useState(false);

  // close the mobile menu whenever the visitor moves to another page
  useEffect(() => { setOpen(false); }, [location.pathname]);

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <>
      <header className="nav">
        <div className="nav-inner">
          <Link to="/" className="brand">Ceylon <span>Guides</span></Link>

          <nav className={`nav-links${open ? ' open' : ''}`}>
            <NavLink to="/packages">Tours</NavLink>
            <NavLink to="/offers">Offers</NavLink>

            {isTourist && <NavLink to="/my-bookings">My bookings</NavLink>}
            {isTourist && <NavLink to="/my-reviews">My reviews</NavLink>}

            {isGuide && <NavLink to="/guide/packages">My tours</NavLink>}
            {isGuide && <NavLink to="/guide/calendar">Calendar</NavLink>}
            {isGuide && <NavLink to="/guide/offers">Promotions</NavLink>}
            {isGuide && <NavLink to="/guide/bookings">Requests</NavLink>}

            {isAdmin && <NavLink to="/guide/bookings">All bookings</NavLink>}
          </nav>

          <span className="nav-spacer" />

          {user ? (
            <>
              <span className="nav-user">{user.full_name}</span>
              <button className="btn btn-ghost btn-sm" onClick={handleLogout}>Log out</button>
            </>
          ) : (
            <div className="row" style={{ gap: 8, flexWrap: 'nowrap' }}>
              <Link to="/login" className="btn btn-ghost btn-sm">Log in</Link>
              <Link to="/register" className="btn btn-sm">Sign up</Link>
            </div>
          )}

          <button
            className="theme-btn"
            onClick={toggle}
            aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
            title={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
          >
            {isDark ? <Sun size={17} /> : <Moon size={17} />}
          </button>

          <button
            className="theme-btn nav-toggle"
            onClick={() => setOpen((v) => !v)}
            aria-label={open ? 'Close menu' : 'Open menu'}
            aria-expanded={open}
          >
            {open ? <X size={18} /> : <List size={18} />}
          </button>
        </div>
      </header>

      <main>{children}</main>
      <Footer />
    </>
  );
}
