import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    // 代理 /fnCaptcha 请求到后端 Spring Boot 应用，避免开发时跨域
    proxy: {
      '/fnCaptcha': {
        target: 'http://localhost:18080',
        changeOrigin: true
      }
    }
  }
})
