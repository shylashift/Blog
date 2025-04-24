// 调试工具
// 用于存储和检查用户角色和邮箱，方便排查权限问题

// 存储当前用户的邮箱
let currentUserEmail: string | null = null

/**
 * 设置当前用户的邮箱地址
 * @param email 用户邮箱
 */
export const setUserEmail = (email: string) => {
  console.log('调试工具: 设置用户邮箱', email)
  currentUserEmail = email
  // 同时保存到本地存储，便于页面刷新后仍能获取
  localStorage.setItem('debug_user_email', email)
}

/**
 * 获取当前用户的邮箱地址
 * @returns 用户邮箱
 */
export const getUserEmail = (): string | null => {
  // 如果内存中没有，尝试从本地存储获取
  if (!currentUserEmail) {
    currentUserEmail = localStorage.getItem('debug_user_email')
  }
  return currentUserEmail
}

/**
 * 检查用户角色，确定是否为管理员
 * 这个函数用于调试权限问题
 * @param roles 用户角色，可能是字符串、字符串数组或其他格式
 * @returns 是否为管理员
 */
export const debugUserRoles = (roles: any): boolean => {
  console.log('调试工具: 检查用户角色', roles, '类型:', typeof roles)
  
  // 获取保存的用户邮箱
  const email = getUserEmail()
  console.log('调试工具: 当前用户邮箱', email)
  
  // 特殊处理: 如果是管理员邮箱，直接返回true
  if (email === 'admin@example.com') {
    console.log('调试工具: 检测到管理员邮箱，强制返回true')
    return true
  }
  
  // 如果是null或undefined，直接返回false
  if (roles === null || roles === undefined) {
    console.log('调试工具: 角色为空，返回false')
    return false
  }
  
  // 如果是字符串，检查是否包含admin
  if (typeof roles === 'string') {
    try {
      // 尝试解析JSON
      if (roles.startsWith('[') && roles.endsWith(']')) {
        const parsedRoles = JSON.parse(roles) as string[]
        console.log('调试工具: 解析JSON字符串为数组', parsedRoles)
        return parsedRoles.some(role => 
          role.toLowerCase().includes('admin') || 
          role.toUpperCase().includes('ROLE_ADMIN')
        )
      }
      // 直接检查字符串
      return roles.toLowerCase().includes('admin') || roles.toUpperCase().includes('ROLE_ADMIN')
    } catch (e) {
      console.error('调试工具: 角色字符串解析错误', e)
      return roles.toLowerCase().includes('admin') || roles.toUpperCase().includes('ROLE_ADMIN')
    }
  }
  
  // 如果是数组，检查是否包含admin角色
  if (Array.isArray(roles)) {
    console.log('调试工具: 角色是数组', roles)
    return roles.some(role => {
      if (typeof role === 'string') {
        return role.toLowerCase().includes('admin') || role.toUpperCase().includes('ROLE_ADMIN')
      }
      return false
    })
  }
  
  // 如果是对象，尝试检查其属性
  if (typeof roles === 'object') {
    console.log('调试工具: 角色是对象', roles)
    // 检查是否有name或authority属性
    if ('name' in roles && typeof roles.name === 'string') {
      return roles.name.toLowerCase().includes('admin')
    }
    if ('authority' in roles && typeof roles.authority === 'string') {
      return roles.authority.toLowerCase().includes('admin')
    }
    
    // 尝试将对象转换为数组处理
    try {
      const roleEntries = Object.entries(roles)
      for (const [key, value] of roleEntries) {
        if (typeof value === 'string' && value.toLowerCase().includes('admin')) {
          return true
        }
        if (key.toLowerCase().includes('admin') && (value === true || value === 'true')) {
          return true
        }
      }
    } catch (e) {
      console.error('调试工具: 角色对象处理错误', e)
    }
  }
  
  console.log('调试工具: 经过所有检查，未找到管理员角色')
  return false
} 