/** SHARED FILE - every route in the application, grouped by functional requirement. */
import { Routes, Route } from 'react-router-dom';

import Layout from './components/Layout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';

// FR-01 Tour Package Management        - Nanayakkara K.U.
import PackageList from './features/packages/PackageList.jsx';
import PackageDetail from './features/packages/PackageDetail.jsx';
import ManagePackages from './features/packages/ManagePackages.jsx';
// FR-02 Availability Calendar          - Ashfak M.A.M.
import ManageCalendar from './features/availability/ManageCalendar.jsx';
// FR-03 Tour Itinerary Management      - Wijesinghe W.M.P.N.
import ManageItinerary from './features/itineraries/ManageItinerary.jsx';
// FR-04 Booking Management             - Dharmapriya R.T.S.
import MyBookings from './features/bookings/MyBookings.jsx';
import GuideBookings from './features/bookings/GuideBookings.jsx';
import CreateBooking from './features/bookings/CreateBooking.jsx';
// FR-05 Special Offers & Promotions    - Hunaif A.A.
import OfferList from './features/offers/OfferList.jsx';
import ManageOffers from './features/offers/ManageOffers.jsx';
// FR-06 Ratings and Reviews            - Sajini S.B.
import MyReviews from './features/reviews/MyReviews.jsx';
import GuideReviews from './features/reviews/GuideReviews.jsx';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* FR-01 */}
        <Route path="/packages" element={<PackageList />} />
        <Route path="/packages/:id" element={<PackageDetail />} />
        <Route path="/guide/packages" element={
          <ProtectedRoute roles={['GUIDE']}><ManagePackages /></ProtectedRoute>} />

        {/* FR-02 */}
        <Route path="/guide/calendar" element={
          <ProtectedRoute roles={['GUIDE']}><ManageCalendar /></ProtectedRoute>} />

        {/* FR-03 */}
        <Route path="/guide/packages/:packageId/itinerary" element={
          <ProtectedRoute roles={['GUIDE']}><ManageItinerary /></ProtectedRoute>} />

        {/* FR-04 */}
        <Route path="/book/:packageId" element={
          <ProtectedRoute roles={['TOURIST']}><CreateBooking /></ProtectedRoute>} />
        <Route path="/my-bookings" element={
          <ProtectedRoute roles={['TOURIST']}><MyBookings /></ProtectedRoute>} />
        <Route path="/guide/bookings" element={
          <ProtectedRoute roles={['GUIDE', 'ADMIN']}><GuideBookings /></ProtectedRoute>} />

        {/* FR-05 */}
        <Route path="/offers" element={<OfferList />} />
        <Route path="/guide/offers" element={
          <ProtectedRoute roles={['GUIDE']}><ManageOffers /></ProtectedRoute>} />

        {/* FR-06 */}
        <Route path="/my-reviews" element={
          <ProtectedRoute roles={['TOURIST']}><MyReviews /></ProtectedRoute>} />
        <Route path="/guides/:guideId/reviews" element={<GuideReviews />} />

        <Route path="*" element={<div className="empty">Page not found.</div>} />
      </Routes>
    </Layout>
  );
}
