import request from '../request'

// ==================== 建议反馈 ====================
export const listSuggestions = (params) => request.get('/suggestions', { params })
export const getSuggestion = (id) => request.get(`/suggestions/${id}`)
export const createSuggestion = (data) => request.post('/suggestions', data)
export const updateSuggestion = (id, data) => request.put(`/suggestions/${id}`, data)
export const replySuggestion = (id, data) => request.post(`/suggestions/${id}/reply`, data)
export const deleteSuggestion = (id) => request.delete(`/suggestions/${id}`)
export const getSuggestionStats = () => request.get('/suggestions/stats')
