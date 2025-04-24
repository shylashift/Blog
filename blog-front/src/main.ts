import './assets/main.css'
import './styles/index.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { initializeUserStore } from '@/stores/user'

// 开启性能监控 (开发时使用)
const isDev = import.meta.env.DEV
if (isDev) {
  console.info('开发模式已启动，开启性能监控')
  window.performance.mark('app-start')
}

// 创建Pinia实例
const pinia = createPinia()

// 创建Vue应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 添加全局错误处理
app.config.errorHandler = (err, instance, info) => {
  console.error('全局错误捕获:', err)
  console.error('错误组件:', instance)
  console.error('错误信息:', info)
}

// 使用插件
app.use(pinia)
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

// 初始化用户状态后再挂载应用
async function initializeApp() {
  try {
    await initializeUserStore()
    app.mount('#app')
  } catch (error) {
    console.error('应用初始化失败:', error)
    // 即使初始化失败也要挂载应用，以便显示错误信息
    app.use(router)
app.mount('#app')
  }
}

initializeApp()

// 开发模式下测量应用启动性能
if (isDev) {
  window.performance.mark('app-mounted')
  window.performance.measure('app-startup', 'app-start', 'app-mounted')
  console.info('应用启动性能:', window.performance.getEntriesByName('app-startup')[0].duration.toFixed(2), 'ms')
}
