import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { login as apiLogin, getCurrentUser } from '@/api/user'
import type { UserInfo, LoginResponse } from '@/api/user'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const initialized = ref(!!localStorage.getItem('token') && !!localStorage.getItem('userInfo'))
  let initializePromise: Promise<void> | null = null

  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
    initialized.value = true
    console.log('用户信息已保存:', info)
  }

  const setToken = (newToken: string) => {
    // 确保 token 包含 Bearer 前缀，并且 Bearer 和 token 之间有空格
    const tokenValue = newToken.startsWith('Bearer ') ? newToken : `Bearer ${newToken}`
    token.value = tokenValue
    localStorage.setItem('token', tokenValue)
    console.log('Token已保存:', tokenValue.substring(0, 20) + '...')
  }

  const clearUser = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    initialized.value = false
    initializePromise = null
    console.log('用户状态已清除')
  }

  const login = async (email: string, password: string) => {
    try {
      console.log('开始登录请求:', { email })
      const response = await apiLogin({ email, password })
      console.log('登录响应:', response)

      if (!response?.token) {
        console.error('登录响应中没有token:', response)
        throw new Error('登录响应中没有token')
      }

      // 设置token
      setToken(response.token)
      
      // 设置用户信息
      setUserInfo({
        userId: response.userId,
        username: response.username,
        email: response.email,
        avatar: response.avatar,
        bio: response.bio,
        roles: response.roles
      })

      initialized.value = true
      console.log('登录成功:', {
        token: token.value.substring(0, 20) + '...',
        userInfo: userInfo.value
      })

      ElMessage.success('登录成功')
      return true
    } catch (error: any) {
      console.error('登录失败:', error)
      clearUser()
      throw error
    }
  }

  const logout = () => {
    clearUser()
    router.push('/login')
    ElMessage.success('已退出登录')
  }

  const validateToken = async () => {
    if (!token.value) {
      console.log('没有token，跳过验证')
      return false
    }

    try {
      console.log('开始验证token:', token.value.substring(0, 20) + '...')
      const userInfoFromLS = JSON.parse(localStorage.getItem('userInfo') || 'null')
      console.log('localStorage中的用户信息:', userInfoFromLS)
      
      // 确保使用正确的API路径
      const response = await getCurrentUser()
      console.log('获取到用户信息:', response)
      
      if (!response) {
        throw new Error('获取用户信息失败')
      }
      
      setUserInfo(response)
      initialized.value = true
      console.log('Token验证成功，用户信息已更新')
      return true
    } catch (error: any) {
      console.error('验证token时发生错误:', error)
      // 只有在明确是 token 相关错误时才清除用户状态
      if (error.response?.status === 401 && 
          error.response?.data?.message?.toLowerCase().includes('token')) {
        clearUser()
      }
      return false
    }
  }

  const initialize = async () => {
    // 如果已经在初始化中，返回现有的 Promise
    if (initializePromise) {
      console.log('初始化已在进行中，等待完成')
      return initializePromise
    }

    // 如果已经初始化完成且有用户信息，直接返回
    if (initialized.value && userInfo.value) {
      console.log('用户状态已初始化且有用户信息，跳过')
      return Promise.resolve()
    }

    console.log('开始初始化用户状态，当前token:', token.value ? (token.value.substring(0, 20) + '...') : 'none')
    
    initializePromise = new Promise<void>(async (resolve) => {
      try {
        if (token.value && userInfo.value) {
          console.log('已有token和用户信息，标记为已初始化')
          // 尝试验证token的有效性
          try {
            await validateToken()
            console.log('验证token成功，确认用户状态有效')
          } catch (error) {
            console.warn('验证token失败，但由于已有用户信息，仍保持已初始化状态')
          }
          initialized.value = true
        } else if (token.value) {
          console.log('发现本地token，开始验证')
          try {
            const success = await validateToken()
            if (!success) {
              console.log('token验证失败，清除用户状态')
              clearUser()
            } else {
              console.log('token验证成功，用户状态已更新')
            }
          } catch (error) {
            console.error('验证token时发生错误', error)
            clearUser()
          }
        } else {
          console.log('没有找到本地token')
          clearUser()
        }
      } catch (error: any) {
        console.error('初始化过程出错:', error)
        clearUser()
      } finally {
        initializePromise = null
        console.log('初始化完成，状态:', {
          hasToken: !!token.value,
          hasUserInfo: !!userInfo.value,
          initialized: initialized.value
        })
        resolve()
      }
    })

    return initializePromise
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    initialized,
    setUserInfo,
    setToken,
    clearUser,
    login,
    logout,
    validateToken,
    initialize
  }
}) 