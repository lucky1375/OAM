import request from '../request'

export const listTasks = (params) => request.get('/tasks', { params })
export const getTaskStats = () => request.get('/tasks/stats')
export const getTask = (id) => request.get(`/tasks/${id}`)
export const createTask = (data) => request.post('/tasks', data)
export const updateTask = (id, data) => request.put(`/tasks/${id}`, data)
export const deleteTask = (id) => request.delete(`/tasks/${id}`)
export const updateTaskProgress = (id, data) => request.post(`/tasks/${id}/progress`, data)
export const assignTask = (id, data) => request.post(`/tasks/${id}/assign`, data)
