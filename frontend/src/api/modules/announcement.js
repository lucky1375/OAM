import request from '../request'

// ==================== 公告 ====================
export const listAnnouncements = (params) => request.get('/announcements', { params })
export const getAnnouncement = (id) => request.get(`/announcements/${id}`)
export const createAnnouncement = (data) => request.post('/announcements', data)
export const updateAnnouncement = (id, data) => request.put(`/announcements/${id}`, data)
export const deleteAnnouncement = (id) => request.delete(`/announcements/${id}`)

// ==================== 阅读 ====================
export const markRead = (id) => request.post(`/announcements/${id}/read`)
export const getReadStats = (id) => request.get(`/announcements/${id}/reads`)
