import dayjs from 'dayjs'

/**
 * 格式化日期
 * @param date 日期字符串
 * @returns 格式化后的日期字符串
 */
export const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
} 