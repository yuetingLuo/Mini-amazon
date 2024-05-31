import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  resolve: {
    alias: {
      vue: '@vue/compat',
    }
  },
  plugins: [
    vue({
      template: {
        compilerOptions: {
          compatConfig: {
            MODE: 2
          }
        }
      }
    }),
  ],

	server: {
    host: "0.0.0.0",
		port: 8080,
		proxy: {
			"^/api": {
				target: "http://localhost:8081",
        changeOrigin: true,
      }
    }
	}
})
