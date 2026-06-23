import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true
})

api.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 401) {
      localStorage.removeItem('ai_cs_user')
      window.location.href = '/login'
      return Promise.reject(new Error('未登录'))
    }
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('ai_cs_user')
      window.location.href = '/login'
    }
    console.warn('API 请求失败:', error.message)
    return Promise.reject(error)
  }
)

export const MOCK_FIRST = false

function withMock(apiCall, mockFn) {
  if (MOCK_FIRST) {
    try {
      return mockFn()
    } catch (e) {
      return Promise.resolve(null)
    }
  }
  return apiCall().catch(() => mockFn())
}

export { api, withMock }
export default api
