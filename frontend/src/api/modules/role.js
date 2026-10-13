import request from '../request'

export function getRoles() {
  return request.get('/roles')
}

export function updateRolePermissions(role, codes) {
  return request.put('/roles/' + role + '/permissions', codes)
}
