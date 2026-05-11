import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  css:{
    preprocessorOptions: {
      scss: {
        additionalData: `
        @use "@/scss/variables.scss" as *;
        @use "@/scss/mixins.scss" as *;
        `
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true
  },
    server: {
      port: 5173,
      proxy: {
          '/api': {
              target: 'http://localhost:8080',
              changeOrigin: true,
              secure: false,
              rewrite: (path: string) => path.replace(/^\/api/, '')
          },
          '/uploads': {
              target: 'http://localhost:8080',
              changeOrigin: true,
              secure: false
          }
      }
    }

})
