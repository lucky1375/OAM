import request from '../request'

export function listEmployees(params) {
  return request.get('/employees', { params })
}

export function createEmployee(data) {
  return request.post('/employees', data)
}

export function updateEmployee(id, data) {
  return request.put('/employees/' + id, data)
}

export function deleteEmployee(id) {
  return request.delete('/employees/' + id)
}
