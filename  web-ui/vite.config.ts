import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tsconfigPaths from 'vite-tsconfig-paths';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tsconfigPaths()],
  server: {
    port: 3000, // Optional: define a port
    open: true, // Optional: open browser on server start
  },
  build: {
    outDir: 'build', // Optional: specify output directory
  },
  define: {
    // Example of defining environment variables (if not using import.meta.env directly)
    // 'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development'),
  }
});