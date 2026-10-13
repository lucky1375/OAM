import request from '../request'

export function listPositions(params) {
  return request.get('/positions', { params })
}

export function createPosition(data) {
  return request.post('/positions', data)
}

export function updatePosition(id, data) {
  return request.put('/positions/' + id, data)
}

export function deletePosition(id) {
  return request.delete('/positions/' + id)
}
