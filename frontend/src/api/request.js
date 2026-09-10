import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：附加 token
service.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

// 响应拦截：统一处理错误
service.interceptors.response.use(
  response => {
    // Blob（文件下载）直接返回原始响应，不做统一解包
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res && !res.success) {
      Message.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    const status = error.response && error.response.status
    if (status === 401) {
      Message.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    } else if (status === 403) {
      Message.error('无权限执行该操作')
    } else {
      const msg = error.response && error.response.data && error.response.data.message
      Message.error(msg || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default service
