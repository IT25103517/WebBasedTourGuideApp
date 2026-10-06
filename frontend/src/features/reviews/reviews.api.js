/** FR-06 Ratings and Reviews - API calls. Member: Sajini S.B. */
import { api } from '../../api/client.js';

export const reviewsApi = {
  listByGuide:   (guideId)   => api.get(`/reviews/guide/${guideId}`),
  listByPackage: (packageId) => api.get(`/reviews/package/${packageId}`),
  listMine:      ()          => api.get('/reviews/my'),
  pending:       ()          => api.get('/reviews/pending'),
  create:        (body)      => api.post('/reviews', body),
  update:        (id, body)  => api.put(`/reviews/${id}`, body),
  remove:        (id)        => api.del(`/reviews/${id}`),
  moderate:      (id, hide)  => api.patch(`/reviews/${id}/moderate`, { is_hidden: hide }),
};
