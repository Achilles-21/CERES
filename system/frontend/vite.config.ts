// @ts-nocheck
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import mkcert from "vite-plugin-mkcert"
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { createSvgIconsPlugin } from "vite-plugin-svg-icons";
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'
import Icons from 'unplugin-icons/vite';
import IconsResolver from 'unplugin-icons/resolver';
import basicSsl from '@vitejs/plugin-basic-ssl'
import path from 'path'

const plugins=[
  vue(),
  basicSsl(),
  AutoImport({
    imports: ['vue', 'vue-router','pinia'],
    dts: './src/types/auto-imports.d.ts',
  }),
  Components({
    dts: './src/types/components.d.ts',
    dirs: ['./src/components'],
    resolvers:[NaiveUiResolver(), IconsResolver()]
  }),
  Icons({ autoInstall: true }),
  createSvgIconsPlugin({
    iconDirs: [path.resolve(process.cwd(), 'src/assets/svg')],
    symbolId: '[name]'
  }),
]
const resolve= {
  alias: {
    '@':  path.resolve(__dirname, './src'),
  },
  extensions: ['.js', '.ts', '.jsx', '.tsx', '.json', '.vue'],
  mainFields: ['module', 'main', 'vue', 'index'],
}
const build = {
  rollupOptions: {
    output: {
      manualChunks(id: string) {
        if (id.includes("node_modules")) {
          return id
            .toString()
            .split("node_modules/")[1]
            .split("/")[0]
            .toString();
        }
      },
      chunkFileNames: (chunkInfo:any) => {
        const facadeModuleId = chunkInfo.facadeModuleId
          ? chunkInfo.facadeModuleId.split("/")
          : [];
        const fileName1 =
          facadeModuleId[facadeModuleId.length - 2] || "[name]";
        return `js/${fileName1}/[name].[hash].js`;
      },
      entryFileNames: "js/[name].[hash].js",
      assetFileNames: "[ext]/[name].[hash:4].[ext]",
    },
  },
}
const esbuild= {
	drop: ['console', 'debugger'],
}

export default defineConfig(({ mode }:{ mode: string }) => {
  const env = loadEnv(mode, __dirname)
  const server = {
    https:false,
    host:'0.0.0.0',
    port: parseInt(env.VITE_APP_START_PORT || "8008"),
    proxy: {
      '/apii': {
        target: env.VITE_JAVA_PROXY || 'http://localhost:18081',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/apii/, '')   
      },
      '/staticc': {
        target: env.VITE_MEDIA_PROXY || 'http://localhost:10008/ceres',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/staticc/, '')   
      },
      '/model-api': {
        target: env.VITE_MODEL_PROXY || 'http://localhost:8000',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/model-api/, ''),
      },
      '/slidegen-api': {
        target: env.VITE_SLIDEGEN_PROXY || 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/slidegen-api/, ''),
      },
      '/slidegen-ws': {
        target: env.VITE_SLIDEGEN_WS_PROXY || 'ws://localhost:8081',
        ws: true,
        rewrite: (path) => path.replace(/^\/slidegen-ws/, ''),
      },
    }
  }
  return {
    plugins,
    resolve,
    server,
    build,
  }
})
