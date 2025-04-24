// 默认头像
export const DEFAULT_AVATAR = 'https://api.dicebear.com/7.x/bottts/png?seed=1234';

// 本地开发环境的默认头像
export const LOCAL_DEFAULT_AVATAR = '/default-avatar.svg';

// 基础URL配置
export const BASE_URL = 'http://localhost:8080';

// API基础URL
export const API_BASE_URL = BASE_URL;

// 上传路径（不包含/api前缀）
export const UPLOAD_PATH = `${BASE_URL}/uploads/`;

// 备用头像URLs，当主要头像无法加载时使用
export const BACKUP_AVATARS = [
  '/default-avatar.svg',
  'https://cdn.jsdelivr.net/gh/yzfly/Avatar@0.0.1/avatar.jpg',
  'https://avatars.githubusercontent.com/u/10208693'
];

// 分页默认值
export const DEFAULT_PAGE_SIZE = 10;

// 其他常量可以在这里添加 