import request from '../request'

// ==================== 报销管理 ====================
export const listExpenses = (params) => request.get('/expenses', { params })
export const getExpenseStats = () => request.get('/expenses/stats')
export const getExpense = (id) => request.get(`/expenses/${id}`)
export const createExpense = (data) => request.post('/expenses', data)
export const updateExpense = (id, data) => request.put(`/expenses/${id}`, data)
export const deleteExpense = (id) => request.delete(`/expenses/${id}`)
export const submitExpense = (id, data) => request.post(`/expenses/${id}/submit`, data)
export const cancelExpense = (id) => request.post(`/expenses/${id}/cancel`)
export const approveExpense = (id, data) => request.post(`/expenses/${id}/approve`, data)
export const rejectExpense = (id, data) => request.post(`/expenses/${id}/reject`, data)

// 发票 OCR 识别（上传图片）
export const recognizeInvoice = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/expenses/invoices/recognize', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 60000
  })
}

// 发票基础校验（重复报销 / 金额一致性）
export const verifyInvoice = (data) => request.post('/expenses/invoices/verify', data)

// 发票识别服务状态
export const getOcrStatus = () => request.get('/expenses/ocr-status')
