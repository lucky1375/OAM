import request from '../request'

// ==================== 流程审批 ====================
export const listWorkflows = (params) => request.get('/workflows', { params })
export const getWorkflow = (id) => request.get(`/workflows/${id}`)
export const getWorkflowStats = () => request.get('/workflows/stats')
export const submitWorkflow = (data) => request.post('/workflows', data)
export const approveWorkflow = (id, data) => request.post(`/workflows/${id}/approve`, data)
export const rejectWorkflow = (id, data) => request.post(`/workflows/${id}/reject`, data)
export const delegateWorkflow = (id, data) => request.post(`/workflows/${id}/delegate`, data)
export const cancelWorkflow = (id) => request.post(`/workflows/${id}/cancel`)

// ==================== 固定审批节点配置 ====================
export const listWorkflowNodes = (type) => request.get('/workflow-nodes', { params: { type } })
export const saveWorkflowNodes = (type, nodes) => request.put(`/workflow-nodes/${type}`, nodes)

// 审批人/转办人候选（复用账号下拉接口）
export const listAccounts = (params) => request.get('/meeting/accounts', { params })
