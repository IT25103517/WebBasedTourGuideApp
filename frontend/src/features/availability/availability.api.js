/** FR-02 Availability Calendar Management - API calls. Member: Ashfak M.A.M. */
import { api } from '../../api/client.js';

export const availabilityApi = {
  listMine:     ()                     => api.get('/availability/my'),
  myCalendar:   (year, month)          => api.get(`/availability/my/calendar?year=${year}&month=${month}`),
  guideCalendar:(guideId, year, month) => api.get(`/availability/calendar/${guideId}?year=${year}&month=${month}`),
  checkDate:    (guideId, date)        => api.get(`/availability/check/${guideId}?date=${date}`),
  create:       (body)                 => api.post('/availability', body),
  update:       (id, body)             => api.put(`/availability/${id}`, body),
  remove:       (id)                   => api.del(`/availability/${id}`),
};
