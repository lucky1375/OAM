import request from '../request'

export function listUsers(params) {
  return request.get('/users', { params })
}

export function createUser(data) {
  return request.post('/users', data)
}

export function updateUser(id, data) {
  return request.put('/users/' + id, data)
}

export function resetPassword(id, newPassword) {
  return request.put('/users/' + id + '/password', { newPassword })
}

export function deleteUser(id) {
  return request.delete('/users/' + id)
}

export function exportUsers() {
  return request.get('/users/export', { responseType: 'blob' })
}
