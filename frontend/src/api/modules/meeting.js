import request from '../request'

// ---------- 会议室 ----------
export function listMeetingRooms(params) {
  return request.get('/meeting-rooms', { params })
}

export function createMeetingRoom(data) {
  return request.post('/meeting-rooms', data)
}

export function updateMeetingRoom(id, data) {
  return request.put('/meeting-rooms/' + id, data)
}

export function deleteMeetingRoom(id) {
  return request.delete('/meeting-rooms/' + id)
}

// ---------- 会议 ----------
export function listMeetings(params) {
  return request.get('/meetings', { params })
}

export function createMeeting(data) {
  return request.post('/meetings', data)
}

export function updateMeeting(id, data) {
  return request.put('/meetings/' + id, data)
}

export function deleteMeeting(id) {
  return request.delete('/meetings/' + id)
}

// ---------- 参会人账号 / 个人分组 ----------
export function listAccounts(params) {
  return request.get('/meeting/accounts', { params })
}

export function listUserGroups() {
  return request.get('/meeting/groups')
}

export function createUserGroup(data) {
  return request.post('/meeting/groups', data)
}

export function updateUserGroup(id, data) {
  return request.put('/meeting/groups/' + id, data)
}

export function deleteUserGroup(id) {
  return request.delete('/meeting/groups/' + id)
}