import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import { getToken } from './auth';
import router from '@/router';

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',  // 修改为相对路径，让 Vite 代理处理
  timeout: 15000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求重试配置
const retryDelay = 1000; // 重试延迟时间
const maxRetries = 3; // 最大重试次数

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = getToken();
    if (token && config.headers) {
      // 检查 token 是否已经包含 Bearer 前缀
      config.headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('[Axios] 请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 对于登录接口，直接返回数据
    if (response.config.url === '/auth/login') {
      return response;
    }

    const res = response.data;
    
    // 如果响应成功但业务状态码不是 200，说明有错误
    if (res.code && res.code !== 200) {
      ElMessage({
        message: res.message || 'Error',
        type: 'error',
        duration: 5 * 1000,
      });

      // 处理特定错误码
      if (res.code === 401) {
        // token 过期或无效
        router.push('/login');
      }
      return Promise.reject(new Error(res.message || 'Error'));
    }
    return res;
  },
  async (error) => {
    console.error('[Axios] 响应错误:', error);
    
    // 如果是取消请求，不进行重试
    if (axios.isCancel(error)) {
      return Promise.reject(error);
    }

    // 如果是网络错误或超时，尝试重试
    if (error.config && (!error.config.retryCount || error.config.retryCount < maxRetries)) {
      // 初始化重试计数
      error.config.retryCount = error.config.retryCount || 0;
      error.config.retryCount++;

      console.log(`[Axios] 正在进行第 ${error.config.retryCount} 次重试...`);

      // 创建新的 Promise 来处理重试
      const backoff = new Promise((resolve) => {
        setTimeout(() => {
          resolve(null);
        }, retryDelay * error.config.retryCount);
      });

      // 等待延迟后重试请求
      await backoff;
      return service(error.config);
    }

    // 获取错误信息
    const errorMessage = error.response?.data?.message || error.message || '请求失败';
    
    // 显示错误消息
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 5 * 1000,
    });

    // 处理特定状态码
    if (error.response?.status === 401) {
      console.log('[Axios] 检测到 401 错误，准备跳转到登录页');
      router.push('/login');
    }

    return Promise.reject(error);
  }
);

// 请求方法封装
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config);
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config);
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config);
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config);
  },

  // 上传文件的专用方法
  upload<T = any>(url: string, file: File, config?: AxiosRequestConfig): Promise<T> {
    const formData = new FormData();
    formData.append('file', file);
    return service.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      ...config,
    });
  },
};

export default service; 