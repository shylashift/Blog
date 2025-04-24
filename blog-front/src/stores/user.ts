import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'
import { ElMessage } from 'element-plus'

export interface UserInfo {
  userId: number  // 保持为userId以维持前端一致性，但在处理数据时会做转换
  username: string
  email: string
  avatar: string | null
  bio: string
  roles: string[]
  disabled: boolean
  muteEndTime: string | null
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(null)
  const userInfo = ref<UserInfo | null>(null)
  const isAuthenticated = ref(false)
  const initialized = ref(false)
  let initializePromise: Promise<boolean> | null = null
  const unreadNotificationCount = ref(0)

  const isLoggedIn = computed(() => {
    return !!(token.value && userInfo.value?.userId)
  })

  function setToken(newToken: string) {
    token.value = newToken
    isAuthenticated.value = true
    localStorage.setItem('token', newToken)
    console.log('[UserStore] Token已更新')
  }

  function clearToken() {
    token.value = null
    isAuthenticated.value = false
    localStorage.removeItem('token')
    console.log('[UserStore] Token已清除')
  }

  function setUserInfo(info: UserInfo) {
    // 确保userId是数字类型
    if (info && (typeof info.userId === 'string' || typeof info.userId === 'undefined')) {
      info.userId = typeof info.userId === 'string' ? parseInt(info.userId, 10) : 0
    }
      userInfo.value = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    console.log('[UserStore] 用户信息已更新:', info)
  }

  function clearUserInfo() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    console.log('[UserStore] 用户信息已清除')
  }

  function updateUserInfo(info: Partial<UserInfo>) {
    if (userInfo.value) {
      // 处理头像URL
      if (info.avatar) {
        // 如果头像URL不是完整的URL，添加域名前缀
        if (!info.avatar.startsWith('http')) {
          // 移除可能存在的/api前缀
          const avatarPath = info.avatar.replace(/^\/api\/uploads\//, '/uploads/')
          const path = avatarPath.startsWith('/') ? avatarPath : '/uploads/' + avatarPath
          info.avatar = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}${path}`
        }
        
        // 添加时间戳防止缓存（确保不重复添加）
        const timestamp = Date.now()
        info.avatar = info.avatar.split('?')[0] + '?t=' + timestamp
      }
      
      userInfo.value = { ...userInfo.value, ...info }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      console.log('用户信息已更新:', userInfo.value)
    }
  }

  function logout() {
    clearToken()
    clearUserInfo()
    router.push('/login')
    ElMessage.success('已退出登录')
  }

  const validateToken = async () => {
    console.log('[UserStore] 开始验证token')
    if (!token.value) {
      console.log('[UserStore] 没有token，需要重新登录')
      return false
    }

    try {
      const response = await fetch('/auth/validate-token', {
        method: 'GET',
        headers: {
          'Authorization': token.value.startsWith('Bearer ') ? token.value : `Bearer ${token.value}`
        }
      })

      if (!response.ok) {
        console.log('[UserStore] Token验证失败，状态码:', response.status)
        if (response.status === 401) {
          clearToken()
          clearUserInfo()
        }
        return false
      }

      console.log('[UserStore] Token验证成功')
      return true
    } catch (error) {
      console.error('[UserStore] 验证token失败:', error)
      return false
    }
  }

  const initialize = async () => {
    console.log('[UserStore] 开始初始化用户状态')
    
    // 如果已经初始化完成，直接返回
    if (initialized.value && userInfo.value) {
      console.log('[UserStore] 已经初始化完成，跳过')
      return true
    }

    // 如果已经有初始化进程在运行，等待它完成
    if (initializePromise) {
      console.log('[UserStore] 等待现有初始化进程完成')
      return await initializePromise
    }

    // 创建新的初始化进程
    initializePromise = (async () => {
      try {
        console.log('[UserStore] 开始新的初始化进程')
        
        // 1. 从localStorage加载数据
        const storedToken = localStorage.getItem('token')
        const storedUserInfo = localStorage.getItem('userInfo')
        
        if (!storedToken || !storedUserInfo) {
          console.log('[UserStore] 未找到存储的用户数据')
          clearUserInfo()
          return false
        }

        // 2. 设置初始token
        setToken(storedToken)

        // 3. 验证token
        const isValid = await validateToken()
        if (!isValid) {
          console.log('[UserStore] token验证失败')
          clearUserInfo()
          return false
        }

        // 4. 设置用户信息
        try {
          const parsedUserInfo = JSON.parse(storedUserInfo)
          setUserInfo(parsedUserInfo)
          console.log('[UserStore] 初始化成功')
          return true
        } catch (error) {
          console.error('[UserStore] 解析用户信息失败:', error)
          clearUserInfo()
          return false
        }
      } catch (error) {
        console.error('[UserStore] 初始化过程出错:', error)
        clearUserInfo()
        return false
      } finally {
        initialized.value = true
        initializePromise = null
      }
    })()

    return await initializePromise
  }

  // 加载时自动初始化
  if (localStorage.getItem('token')) {
    console.log('[UserStore] 检测到存储的token，开始自动初始化')
    initialize()
  }

  // 减少未读消息计数
  const decrementUnreadCount = () => {
    if (unreadNotificationCount.value > 0) {
      unreadNotificationCount.value--
    }
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    isLoggedIn,
    initialized,
    unreadNotificationCount,
    decrementUnreadCount,
    setToken,
    clearToken,
    setUserInfo,
    clearUserInfo,
    updateUserInfo,
    logout,
    validateToken,
    initialize
  }
}) 

// 创建一个初始化函数，确保store在使用前已经初始化
export async function initializeUserStore() {
  const store = useUserStore()
  if (!store.initialized && localStorage.getItem('token')) {
    await store.initialize()
  }
  return store
} 