/** FR-05 Special Offers & Promotions - API calls. Member: Hunaif A.A. */
import { api } from '../../api/client.js';

export const offersApi = {
  listActive: ({ packageId, guideId } = {}) => {
    const q = new URLSearchParams();
    if (packageId) q.set('packageId', packageId);
    if (guideId) q.set('guideId', guideId);
    return api.get(`/offers${q.toString() ? `?${q}` : ''}`);
  },
  listMine:  ()                       => api.get('/offers/my'),
  getOne:    (id)                     => api.get(`/offers/${id}`),
  create:    (body)                   => api.post('/offers', body),
  update:    (id, body)               => api.put(`/offers/${id}`, body),
  remove:    (id)                     => api.del(`/offers/${id}`),
  expireOld: ()                       => api.post('/offers/expire', {}),
  preview:   (offerId, packageId, groupSize = 1) =>
    api.get(`/offers/${offerId}/preview?package_id=${packageId}&group_size=${groupSize}`),
};
