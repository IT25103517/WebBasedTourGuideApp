/** FR-01 Tour Package Management - API calls. Member: Nanayakkara K.U. */
import { api } from '../../api/client.js';

export const packagesApi = {
  listPublished: (params = {}) => {
    const q = new URLSearchParams(Object.entries(params).filter(([, v]) => v !== '' && v != null));
    return api.get(`/packages${q.toString() ? `?${q}` : ''}`);
  },
  listMine: ()          => api.get('/packages/my'),
  getOne:   (id)        => api.get(`/packages/${id}`),
  create:   (body)      => api.post('/packages', body),
  update:   (id, body)  => api.put(`/packages/${id}`, body),
  remove:   (id)        => api.del(`/packages/${id}`),
};
