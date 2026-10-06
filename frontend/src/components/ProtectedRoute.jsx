/** SHARED FILE - blocks a page unless the user is logged in with the right role. */
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { Empty } from './Ui.jsx';

export default function ProtectedRoute({ roles, children }) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="page"><div className="shell">
        <div className="skeleton" style={{ height: 220, borderRadius: 'var(--r-lg)' }} />
      </div></div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  if (roles && !roles.includes(user.role)) {
    return (
      <div className="page"><div className="shell">
        <Empty title="This page is not for your account type">
          You are signed in as a {user.role.toLowerCase()}. This page is for{' '}
          {roles.map((r) => r.toLowerCase()).join(' or ')} accounts.
        </Empty>
      </div></div>
    );
  }

  return children;
}
