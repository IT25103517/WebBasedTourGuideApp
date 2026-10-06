/** FR-04 Booking Management - API calls. Member: Dharmapriya R.T.S. */
import { api } from '../../api/client.js';

export const bookingsApi = {
  create:       (body)          => api.post('/bookings', body),
  list:         (params = {})   => {
    const q = new URLSearchParams(Object.entries(params).filter(([, v]) => v));
    return api.get(`/bookings${q.toString() ? `?${q}` : ''}`);
  },
  stats:        ()              => api.get('/bookings/stats'),
  getOne:       (id)            => api.get(`/bookings/${id}`),
  update:       (id, body)      => api.put(`/bookings/${id}`, body),
  updateStatus: (id, status)    => api.patch(`/bookings/${id}/status`, { status }),
  cancel:       (id)            => api.patch(`/bookings/${id}/cancel`, {}),
  remove:       (id)            => api.del(`/bookings/${id}`),
};
