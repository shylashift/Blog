import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建axios实例
const api = axios.create({
  baseURL: '/api',  // 移除 /api 前缀
  timeout: 30000  // 增加超时时间到60秒
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
    if (!(config.data instanceof FormData)) {
      // 如果不是FormData，才设置Content-Type
      config.headers['Content-Type'] = 'application/json'
    }
    
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
    }

    return config
  },
  error => {
    console.error('[API Request Error]', error)
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

    // 打印响应信息
    console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, {
      status: response.status,
      data: response.data
    });

    // 如果是文件上传响应，直接返回完整响应数据
    if (response.config.url?.includes('/avatar')) {
      return response.data;
    }

    // 如果是AI相关的响应，返回响应数据本身，不要额外处理
    if (response.config.url?.includes('/ai/')) {
      console.log('处理AI响应:', response.data)
      return response;  // 返回完整的响应对象，让调用者处理数据结构
    }

    // 如果响应数据中有 data 字段，返回 data 字段的内容
    if (response.data?.data) {
      return response.data.data
    }
    
    // 其他情况返回原始数据
    return response.data
  },
  async error => {
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
      // 检查错误信息是否包含"expired"或"JWT expired"
      const isTokenExpired = 
        error.response?.data?.message?.toLowerCase().includes('expired') ||
        error.response?.data?.message?.toLowerCase().includes('jwt expired') ||
        error.response?.data?.error?.toLowerCase().includes('expired') ||
        error.response?.data?.error?.toLowerCase().includes('jwt expired')
      
      // 如果是token过期错误，清除用户状态并跳转到登录页
      if (isTokenExpired) {
        console.log('Token 已过期，清除用户状态')
        userStore.clearUserInfo()
        
        // 如果不是公开路径的请求，显示消息并跳转
        if (!isPublicPath(error.config?.url, error.config?.method)) {
          ElMessage.warning('登录已过期，请重新登录')
          // 如果当前不在登录页，跳转到登录页
          if (router.currentRoute.value.path !== '/login') {
            router.push('/login')
          }
        }
      }
      // 如果是其他token相关错误
      else if (error.response?.data?.message?.toLowerCase().includes('token')) {
        console.log('Token无效，清除用户状态')
        userStore.clearUserInfo()
        // 如果不是公开路径的请求，显示消息并跳转
        if (!isPublicPath(error.config?.url, error.config?.method)) {
          ElMessage.error('登录状态无效，请重新登录')
          if (router.currentRoute.value.path !== '/login') {
            router.push('/login')
          }
        }
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
      if (error.config?.url === '/users/current' || error.config?.url === '/users/me') {
        console.log('获取用户信息失败，清除用户状态')
        userStore.clearUserInfo()
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