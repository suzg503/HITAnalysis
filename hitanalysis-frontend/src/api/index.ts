import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useUserStore } from '@/stores/user'
import { TOKEN_KEY } from '@/constants'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, message, data } = response.data

    if (code === 0) {
      return data
    }

    // Business error
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    const { response } = error

    if (response) {
      const { status } = response

      if (status === 401) {
        // Token expired or invalid
        const userStore = useUserStore()
        userStore.logout()
        router.push({ name: 'Login' })
        ElMessage.error('登录已过期，请重新登录')
      } else if (status === 403) {
        // Permission denied (D3)
        router.push({ name: 'PermissionDenied' })
        ElMessage.error('权限不足，无法访问')
      } else if (status === 500) {
        ElMessage.error('服务器错误，请稍后重试')
      } else {
        ElMessage.error(response.data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

export default service

export const request = <T = any>(config: AxiosRequestConfig): Promise<T> => {
  return service.request<any, T>(config)
}

export const get = <T = any>(url: string, params?: any): Promise<T> => {
  return request({ method: 'GET', url, params })
}

export const post = <T = any>(url: string, data?: any): Promise<T> => {
  return request({ method: 'POST', url, data })
}

export const put = <T = any>(url: string, data?: any): Promise<T> => {
  return request({ method: 'PUT', url, data })
}

export const del = <T = any>(url: string): Promise<T> => {
  return request({ method: 'DELETE', url })
}