import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { login as apiLogin } from '@/api/user'
import api from '@/api/axios'

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
      const data = await apiLogin({ username, password })
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
      const response = await api.get('/users/me', {
        headers: {
          'Authorization': `Bearer ${token.value}`
        }
      })
      userInfo.value = response.data
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