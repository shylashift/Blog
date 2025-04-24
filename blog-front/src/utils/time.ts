/**
 * 将时间转换为友好的显示格式
 * @param dateString ISO格式的日期字符串
 * @returns 格式化后的时间字符串
 * 
 * 规则：
 * 1. 1分钟内：显示"刚刚"
 * 2. 1小时内：显示"xx分钟前"
 * 3. 24小时内：显示"xx小时前"
 * 4. 7天内：显示"xx天前"
 * 5. 超过7天：显示具体的日期和时间
 */
export function formatTime(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  // 转换为分钟
  const minutes = Math.floor(diff / (1000 * 60));
  if (minutes < 1) {
    return '刚刚';
  }

  // 转换为小时
  const hours = Math.floor(minutes / 60);
  if (hours < 1) {
    return `${minutes}分钟前`;
  }

  // 转换为天
  const days = Math.floor(hours / 24);
  if (days < 1) {
    return `${hours}小时前`;
  }

  if (days < 7) {
    return `${days}天前`;
  }

  // 超过7天，显示具体日期
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');

  const currentYear = now.getFullYear();
  if (year === currentYear) {
    // 如果是当年，不显示年份
    return `${month}-${day} ${hour}:${minute}`;
  }

  return `${year}-${month}-${day} ${hour}:${minute}`;
} 