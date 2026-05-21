import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig(({ mode }) => {
  const projectRoot = fileURLToPath(new URL('..', import.meta.url))
  const env = loadEnv(mode, projectRoot, '')
  const vitePort = Number(env.VITE_PORT ?? 5173)
  const springBootPort = Number(env.SPRING_BOOT_PORT ?? 8080)

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: vitePort,
      strictPort: true,
      proxy: {
        '/api': {
          target: `http://localhost:${springBootPort}`,
          changeOrigin: true,
          secure: false
        }
      }
    },
    build: {
      outDir: 'dist',
      emptyOutDir: true,
      assetsDir: 'assets',
      sourcemap: false
    }
  }
})
