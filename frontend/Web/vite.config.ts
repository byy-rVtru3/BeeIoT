import path from 'path';
import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_');
  const proxyTarget = env.VITE_API_PROXY_TARGET || 'http://62.109.16.63';

  return {
    plugins: [react()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      proxy: {
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          secure: false,
          configure: (proxy) => {
            proxy.on('proxyReq', (_proxyReq, req) => {
              console.log(`[proxy] --> ${req.method} ${req.url}  =>  ${proxyTarget}${req.url}`);
            });
            proxy.on('proxyRes', (proxyRes, req) => {
              console.log(`[proxy] <-- ${proxyRes.statusCode} ${req.url}`);
            });
            proxy.on('error', (err, req) => {
              console.error(`[proxy] ERROR on ${req.url}:`, err.message);
              console.error(`[proxy] Target: ${proxyTarget}`);
              console.error(`[proxy] Code: ${(err as NodeJS.ErrnoException).code}`);
            });
          },
        },
      },
    },
  };
});
