import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',  // 添加 /api 前缀
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// 判断是否是公开路径
const isPublicPath = (url: string | undefined, method?: string) => {
  if (!url) return false
  
  // 登录注册相关的公开路径
  const publicPaths = [
    '/auth/login',
    '/auth/register'
  ]

  // 只有 GET 请求的文章列表和详情是公开的
  if (method?.toLowerCase() === 'get') {
    // 文章列表
    if (url === '/posts') return true
    // 文章详情
    if (url.match(/^\/posts\/\d+$/)) return true
    // 文章标签
    if (url === '/posts/tags') return true
    // 根据标签获取文章
    if (url.match(/^\/posts\/bytags/)) return true
    // 获取特定用户信息
    if (url.match(/^\/users\/\d+$/)) return true
  }

  // 完全公开的路径
  if (publicPaths.some(p => url.startsWith(p))) {
    return true
  }

  // 注意：/users/me 和 /users/posts 不是公开路径，需要认证
  return false
}

// 请求拦截器
api.interceptors.request.use(
  async (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    
    // 确保用户状态已初始化
    if (!userStore.initialized) {
      await userStore.initialize()
    }
    
    // 设置通用请求头
    Object.assign(config.headers, {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Cache-Control': 'no-cache',
      'Pragma': 'no-cache'
    })
    
    // 获取最新的token
    let token = localStorage.getItem('token')
    
    // 如果localStorage中没有但store中有token
    if (!token && userStore.token) {
      token = userStore.token
      localStorage.setItem('token', token)
    }
    
    // 如果有token，就添加到所有请求中
    if (token) {
      // 确保token以Bearer开头
      const authToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`
      config.headers.Authorization = authToken
      
      // 日志记录
      console.log('请求添加Token:', {
        url: config.url,
        token: authToken.substring(0, 20) + '...',
        roles: userStore.userInfo?.roles
      })
    } else {
      console.log('请求无Token:', config.url)
    }

    // 管理员API调用特别处理，加强日志
    if (config.url?.includes('/admin/')) {
      console.log('管理员API调用:', {
        url: config.url,
        method: config.method,
        headers: config.headers,
        hasToken: !!config.headers.Authorization,
        userRoles: userStore.userInfo?.roles,
        isAdmin: userStore.userInfo?.roles?.includes('ROLE_ADMIN')
      })
    }

    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('收到响应:', {
      url: response.config.url,
      status: response.status,
      rawData: response.data,
      hasToken: !!response.config.headers?.Authorization
    })

    // 如果响应数据本身就是一个对象，直接返回
    if (typeof response.data === 'object' && !response.data?.data) {
      return response.data
    }
    // 如果响应数据中有 data 字段，返回 data 字段的内容
    if (response.data?.data) {
      return response.data.data
    }
    // 其他情况返回原始数据
    return response.data
  },
  error => {
    console.error('响应错误:', {
      url: error.config?.url,
      method: error.config?.method,
      status: error.response?.status,
      data: error.response?.data,
      headers: error.config?.headers,
      message: error.message,
      hasToken: !!error.config?.headers?.Authorization,
      rawError: error
    })
    
    const userStore = useUserStore()

    // 处理 401 错误
    if (error.response?.status === 401) {
      // 如果是 token 相关错误，清除用户状态
      if (error.response?.data?.message?.toLowerCase().includes('token')) {
        console.log('Token 已过期或无效，清除用户状态')
        userStore.clearUser()
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      }
      // 如果是登录失败，只提示错误
      else if (error.config?.url?.includes('/auth/login')) {
        ElMessage.error('登录失败，请检查邮箱和密码')
      }
      // 其他 401 错误
      else {
        ElMessage.error('没有权限执行此操作')
      }
    }
    // 处理 400 错误
    else if (error.response?.status === 400) {
      console.error('请求参数错误，详细信息:', {
        url: error.config?.url,
        data: error.response?.data,
        headers: error.config?.headers
      })
      // 如果是 getCurrentUser 请求
      if (error.config?.url === '/users/current') {
        console.log('获取用户信息失败，清除用户状态')
        userStore.clearUser()
      }
      ElMessage.error(error.response.data?.message || '请求参数错误')
    }
    // 处理 500 错误
    else if (error.response?.status === 500) {
      console.error('服务器错误，详细信息:', {
        url: error.config?.url,
        method: error.config?.method,
        data: error.response?.data,
        headers: error.config?.headers
      })
      ElMessage.error('服务器内部错误，请稍后重试')
    }
    // 处理服务器连接错误
    else if (!error.response) {
      ElMessage.error('无法连接到服务器，请稍后重试')
    }
    // 处理其他错误
    else {
      ElMessage.error(error.response.data?.message || '请求失败')
    }

    return Promise.reject(error)
  }
)

export default api  