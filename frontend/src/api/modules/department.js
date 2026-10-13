import request from '../request'

export function getDepartmentTree() {
  return request.get('/departments/tree')
}

export function getDepartmentFlat() {
  return request.get('/departments/flat')
}

export function createDepartment(data) {
  return request.post('/departments', data)
}

export function updateDepartment(id, data) {
  return request.put('/departments/' + id, data)
}

export function deleteDepartment(id) {
  return request.delete('/departments/' + id)
}
