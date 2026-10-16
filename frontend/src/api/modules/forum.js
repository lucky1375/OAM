import request from '../request'

// ==================== 版块 ====================
export const listSections = () => request.get('/forum/sections')
export const createSection = (data) => request.post('/forum/sections', data)
export const updateSection = (id, data) => request.put(`/forum/sections/${id}`, data)
export const deleteSection = (id) => request.delete(`/forum/sections/${id}`)

// ==================== 帖子 ====================
export const listPosts = (params) => request.get('/forum/posts', { params })
export const getPost = (id) => request.get(`/forum/posts/${id}`)
export const createPost = (data) => request.post('/forum/posts', data)
export const updatePost = (id, data) => request.put(`/forum/posts/${id}`, data)
export const deletePost = (id) => request.delete(`/forum/posts/${id}`)

// ==================== 评论 / 点赞 ====================
export const addComment = (id, data) => request.post(`/forum/posts/${id}/comments`, data)
export const deleteComment = (id) => request.delete(`/forum/comments/${id}`)
export const toggleLike = (id) => request.post(`/forum/posts/${id}/like`)
