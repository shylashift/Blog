import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

interface UserInfo {
  id: number
  username: string
  email: string
  role: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<UserInfo | null>(null)

  const isAuthenticated = computed(() => !!token.value && !!userInfo.value)

  // 设置token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 设置用户信息
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
  }

  // 登录
  const login = async (username: string, password: string) => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
      })

      if (!response.ok) {
        throw new Error('登录失败')
      }

      const data = await response.json()
      setToken(data.token)
      setUserInfo(data.userInfo)
      await fetchUserInfo()
      
      ElMessage.success('登录成功')
      return true
    } catch (error) {
      console.error('登录错误:', error)
      ElMessage.error('登录失败，请检查用户名和密码')
      return false
    }
  }

  // 登出
  const logout = () => {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    ElMessage.success('已退出登录')
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    if (!token.value) return

    try {
      const response = await fetch('/api/auth/me', {
        headers: {
          'Authorization': `Bearer ${token.value}`
        }
      })

      if (!response.ok) {
        throw new Error('获取用户信息失败')
      }

      userInfo.value = await response.json()
    } catch (error) {
      console.error('获取用户信息错误:', error)
      token.value = null
      userInfo.value = null
      localStorage.removeItem('token')
    }
  }

  return {
    token,
    userInfo,
    isAuthenticated,
    login,
    logout,
    fetchUserInfo
  }
}) 