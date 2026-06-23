import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import obfuscatorPlugin from 'rollup-plugin-obfuscator'

export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        cookieDomainRewrite: { '*': '' }
      },
      '/ws': {
        target: 'ws://localhost:8082',
        ws: true
      }
    }
  },
  build: {
    target: 'es2015',
    cssTarget: 'chrome61',
    modulePreload: false,
    minify: 'terser',
    terserOptions: {
      compress: { drop_console: true, drop_debugger: true }
    },
    rollupOptions: {
      plugins: []
    }
  },
})
