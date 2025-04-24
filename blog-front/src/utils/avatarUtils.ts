import { DEFAULT_AVATAR, BASE_URL ,BACKUP_AVATARS } from './constants'
import { ref } from 'vue'

// 使用Vue的响应式系统来管理备用头像索引
const backupAvatarIndex = ref(0)

/**
 * 获取下一个备用头像URL
 * @returns 备用头像URL
 */
export const getNextBackupAvatar = (): string => {
  const avatar = BACKUP_AVATARS[backupAvatarIndex.value]
  backupAvatarIndex.value = (backupAvatarIndex.value + 1) % BACKUP_AVATARS.length
  return avatar
}

/**
 * 获取备用头像
 * @returns 备用头像URL
 */
export const getBackupAvatar = (): string => {
  return getNextBackupAvatar()
}

/**
 * 为URL添加时间戳
 * @param url URL字符串
 * @returns 添加时间戳后的URL
 */
export const addTimestamp = (url: string): string => {
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}t=${Date.now()}`
}

/**
 * 获取完整的头像URL
 * @param avatar 头像路径或URL
 * @param addCache 是否添加时间戳防止缓存，默认为true
 * @returns 完整的头像URL
 */
export const getAvatarUrl = (avatar?: string | null, addCache = true): string => {
  // 如果没有提供头像，返回默认头像
  if (!avatar) {
    return DEFAULT_AVATAR
  }

  try {
    // 检查是否已经是完整的URL
    new URL(avatar)
    return addCache ? addTimestamp(avatar) : avatar
  } catch {
    // 不是完整URL，需要处理
    let fullUrl = ''
    
    // 移除开头的斜杠和可能存在的api前缀
    const cleanPath = avatar.replace(/^\/?(api\/)?uploads\//, '')
    
    // 构建完整的URL
    fullUrl = `${BASE_URL}/uploads/${cleanPath}`
    
    // 根据addCache参数决定是否添加时间戳
    return addCache ? addTimestamp(fullUrl) : fullUrl
  }
}