import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    // Toute requete vers /api est transmise au backend Spring Boot
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})