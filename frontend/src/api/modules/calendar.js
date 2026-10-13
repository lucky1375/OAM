import request from '../request'

export function listCalendars(params) {
  return request.get('/calendars', { params })
}

export function createCalendar(data) {
  return request.post('/calendars', data)
}

export function updateCalendar(id, data) {
  return request.put('/calendars/' + id, data)
}

export function deleteCalendar(id) {
  return request.delete('/calendars/' + id)
}