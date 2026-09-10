import request from '../request'

export function getPermissionTree() {
  return request.get('/permissions/tree')
}

export function getAssignedPermissions(userId) {
  return request.get('/permissions/users/' + userId)
}

export function updatePermissions(userId, permissions) {
  return request.put('/permissions/users/' + userId, { permissions })
}

export function listUsers() {
  return request.get('/users')
}
