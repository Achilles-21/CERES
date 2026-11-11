import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  return {
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5175,
    hmr: {
      clientPort: 5175,
    },
    proxy: {
      '/ws': {
        target: env.VITE_SLIDEGEN_WS_PROXY || 'ws://localhost:8081',
        ws: true
      },
      '/api': {
        target: env.VITE_SLIDEGEN_PROXY || 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
}
})
