import axios from 'axios'
import toast from 'react-hot-toast'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

// ─── Request Interceptor ─────────────────────────────────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ─── Response Interceptor ────────────────────────────────────────────────────
api.interceptors.response.use(
  (response) => {
    // Unwrap ApiResponse<T> → return .data field
    // Handle both { success, message, data } shape and plain responses
    if (response.data && typeof response.data === 'object' && 'data' in response.data) {
      return response.data.data
    }
    return response.data
  },
  (error) => {
    const status  = error.response?.status
    const payload = error.response?.data

    if (status === 401) {
      localStorage.clear()
      window.location.href = '/login'
      return Promise.reject(error)
    }

    if (status === 403) {
      toast.error('Access denied. Insufficient permissions.')
      return Promise.reject(error)
    }

    if (status === 404) {
      toast.error(payload?.message || 'Resource not found.')
      return Promise.reject(error)
    }

    if (status === 409) {
      toast.error(payload?.message || 'Resource already exists.')
      return Promise.reject(error)
    }

    if (status === 400) {
      // Let the calling component handle validation errors
      return Promise.reject(error)
    }

    if (status === 500) {
      toast.error('Something went wrong. Please try again.')
      return Promise.reject(error)
    }

    return Promise.reject(error)
  }
)

export default api