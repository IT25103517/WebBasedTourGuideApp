/** FR-03 Tour Itinerary Management - API calls. Member: Wijesinghe W.M.P.N. */
import { api } from '../../api/client.js';

export const itinerariesApi = {
  listByPackage:  (packageId)          => api.get(`/itineraries/package/${packageId}`),
  getDay:         (id)                 => api.get(`/itineraries/${id}`),
  createDay:      (body)               => api.post('/itineraries', body),
  updateDay:      (id, body)           => api.put(`/itineraries/${id}`, body),
  deleteDay:      (id)                 => api.del(`/itineraries/${id}`),
  addActivity:    (dayId, body)        => api.post(`/itineraries/${dayId}/activities`, body),
  updateActivity: (activityId, body)   => api.put(`/itineraries/activities/${activityId}`, body),
  deleteActivity: (activityId)         => api.del(`/itineraries/activities/${activityId}`),
  replaceAll:     (packageId, days)    => api.put(`/itineraries/package/${packageId}`, { days }),
};
