import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers';
import viteCompression from 'vite-plugin-compression';
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
                manualChunks: function (id) {
                    if (id.includes('node_modules')) {
                        return 'vendor';
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
                rewrite: function (path) { return path.replace(/^\/api/, ''); },
                configure: function (proxy, _options) {
                    proxy.on('proxyReq', function (proxyReq, req) {
                        // 不修改Content-Type，让浏览器处理
                        var originalHeaders = req.headers;
                        // 设置所有原始请求头，除了Content-Type
                        Object.keys(originalHeaders).forEach(function (key) {
                            if (key.toLowerCase() !== 'content-type' && key.toLowerCase() !== 'host') {
                                var value = originalHeaders[key];
                                if (value !== undefined) {
                                    proxyReq.setHeader(key, value);
                                }
                            }
                        });
                        // 特别处理 Authorization 头
                        var token = originalHeaders.authorization;
                        if (token) {
                            proxyReq.setHeader('Authorization', token);
                        }
                        // 对于非FormData请求，处理请求体
                        if (['POST', 'PUT'].includes(req.method || '') &&
                            req.headers['content-type'] === 'application/json' &&
                            req.body) {
                            var bodyData = JSON.stringify(req.body);
                            proxyReq.setHeader('Content-Length', Buffer.byteLength(bodyData));
                            proxyReq.write(bodyData);
                        }
                    });
                    proxy.on('proxyRes', function (proxyRes, req) {
                        var _a;
                        var statusCode = proxyRes.statusCode;
                        // 特别处理管理员请求的响应
                        if ((_a = req.url) === null || _a === void 0 ? void 0 : _a.includes('/admin/')) {
                            console.log('管理员API响应:', {
                                url: req.url,
                                method: req.method,
                                status: statusCode
                            });
                        }
                        // 记录重要状态码
                        if (statusCode === 401 || statusCode === 403) {
                            console.log("\u6536\u5230".concat(statusCode, "\u9519\u8BEF:"), {
                                originalUrl: req.url,
                                originalMethod: req.method,
                                originalHeaders: req.headers,
                                responseHeaders: proxyRes.headers,
                                responseStatus: proxyRes.statusMessage
                            });
                        }
                    });
                }
            }
        }
    }
});
