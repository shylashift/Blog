import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import viteCompression from 'vite-plugin-compression'
import type { ServerOptions } from 'http-proxy'
import type { IncomingMessage } from 'http'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts',
    }),
    viteCompression({
      verbose: true,
      disable: false,
      threshold: 10240,
      algorithm: 'gzip',
      ext: '.gz',
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  optimizeDeps: {
    include: ['dayjs']
  },
  build: {
    chunkSizeWarningLimit: 1500,
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules')) {
            return 'vendor'
          }
        }
      }
    }
  },
  server: {
    hmr: { overlay: false },
    cors: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, ''),
        configure: (proxy, _options) => {
          proxy.on('proxyReq', (proxyReq, req: IncomingMessage & { body?: any }) => {
            // 保留原始请求头
            const originalHeaders = req.headers
            
            // 设置所有原始请求头
            Object.keys(originalHeaders).forEach(key => {
              const value = originalHeaders[key]
              if (key.toLowerCase() !== 'host' && value !== undefined) { // 不设置 host 头，且确保值不为 undefined
                proxyReq.setHeader(key, value)
              }
            })

            // 特别处理 Authorization 头
            const token = originalHeaders.authorization
            if (token) {
              proxyReq.setHeader('Authorization', token)
              console.log('添加Authorization头:', token.substring(0, 20) + '...')
            }

            // 特别处理管理员请求
            if (req.url?.includes('/admin/')) {
              console.log('管理员API请求:', {
                url: req.url,
                method: req.method,
                headers: proxyReq.getHeaders()
              })
            }

            // 处理 POST/PUT 请求的请求体
            if (['POST', 'PUT'].includes(req.method || '') && req.body) {
              const bodyData = JSON.stringify(req.body)
              proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData))
              proxyReq.write(bodyData)
            }
          })

          proxy.on('proxyRes', (proxyRes, req) => {
            const statusCode = proxyRes.statusCode
            
            // 特别处理管理员请求的响应
            if (req.url?.includes('/admin/')) {
              console.log('管理员API响应:', {
                url: req.url,
                method: req.method,
                status: statusCode
              })
            }

            // 记录重要状态码
            if (statusCode === 401 || statusCode === 403) {
              console.log(`收到${statusCode}错误:`, {
                originalUrl: req.url,
                originalMethod: req.method,
                originalHeaders: req.headers,
                responseHeaders: proxyRes.headers,
                responseStatus: proxyRes.statusMessage
              })
            }
          })
        }
      }
    }
  }
})
