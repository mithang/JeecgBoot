import type { UserConfig, ConfigEnv } from 'vite';
import pkg from './package.json';
import dayjs from 'dayjs';
import { loadEnv } from 'vite';
import { resolve } from 'path';
import { generateModifyVars } from './build/generate/generateModifyVars';
import { createProxy } from './build/vite/proxy';
import { wrapperEnv } from './build/utils';
import { createVitePlugins } from './build/vite/plugin';
import { OUTPUT_DIR } from './build/constant';

function pathResolve(dir: string) {
  return resolve(process.cwd(), '.', dir);
}

const { dependencies, devDependencies, name, version } = pkg;
const __APP_INFO__ = {
  pkg: { dependencies, devDependencies, name, version },
  lastBuildTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
};

export default async ({ command, mode }: ConfigEnv): Promise<UserConfig> => {
  const root = process.cwd();

  const env = loadEnv(mode, root);

  // The boolean type read by loadEnv is a string. This function can be converted to boolean type
  const viteEnv = wrapperEnv(env);

  const { VITE_PORT, VITE_PUBLIC_PATH, VITE_PROXY } = viteEnv;

  const isBuild = command === 'build';

  const serverOptions: Recordable = {}

  // Khi JEECG là ứng dụng con qiankun, cần bật CORS và chỉ định origin
  const {VITE_GLOB_QIANKUN_MICRO_APP_NAME, VITE_GLOB_QIANKUN_MICRO_APP_ENTRY} = viteEnv;
  const isQiankunMicro = VITE_GLOB_QIANKUN_MICRO_APP_NAME != null && VITE_GLOB_QIANKUN_MICRO_APP_NAME !== '';
  if (isQiankunMicro && !isBuild) {
    serverOptions.cors = true;
    serverOptions.origin = VITE_GLOB_QIANKUN_MICRO_APP_ENTRY!.split('/').slice(0, 3).join('/');
  }

  console.log('[init] Start Port: ', VITE_PORT);
  console.debug('[init] Vite Proxy Config: ', VITE_PROXY);

  return {
    base: isQiankunMicro ? VITE_GLOB_QIANKUN_MICRO_APP_ENTRY : VITE_PUBLIC_PATH,
    root,
    resolve: {
      alias: [
        // Chuyển hướng runtime phía client của @rys-fe/vite-plugin-theme sang phiên bản tích hợp sẵn (tương thích vite 8); dùng RegExp để khớp chính xác, tránh bị alias của cấp cha nuốt mất
        {
          find: /^@rys-fe\/vite-plugin-theme\/es\/client$/,
          replacement: pathResolve('build/vite/plugin/theme-plugin/client/client'),
        },
        {
          find: /^@rys-fe\/vite-plugin-theme\/es\/colorUtils$/,
          replacement: pathResolve('build/vite/plugin/theme-plugin/client/colorUtils'),
        },
        {
          find: /^@rys-fe\/vite-plugin-theme$/,
          replacement: pathResolve('build/vite/plugin/theme-plugin/index'),
        },
        {
          find: 'vue-i18n',
          replacement: 'vue-i18n/dist/vue-i18n.cjs.js',
        },
        // /@/xxxx, @/xxxx => src/xxxx
        {
          find: /\/?@\//,
          replacement: pathResolve('src') + '/',
        },
        // /#/xxxx, #/xxxx => types/xxxx
        {
          find: /\/?#\//,
          replacement: pathResolve('types') + '/',
        },
      ],
      // Tránh việc nhiều bản sao của hệ sinh thái vue bị phân tích/tiền xây dựng trùng lặp, giảm chi phí phát hiện và chuyển đổi dependency
      dedupe: ['vue', 'vue-router', 'pinia', '@vue/shared'],
    },
    server: {
      // Listening on all local IPs
      host: true,
      // @ts-ignore
      https: false,
      port: VITE_PORT,
      proxy: createProxy(VITE_PROXY),
      ...serverOptions,
      // Tiền xây dựng một số trang entry thường dùng khi khởi động, truy cập sẽ nhanh hơn
      warmup: {
        clientFiles: [
          './src/main.ts',
          './src/App.vue',
          './src/views/system/loginmini/MiniLogin.vue',
          'src/layouts/default/index.vue',
          'src/views/dashboard/Analysis/index.vue',
          'src/views/dashboard/workbench/index.vue',
          'src/views/system/user/index.vue',
          'src/views/system/role/index.vue',
          'src/views/system/depart/index.vue',
          'src/views/system/menu/index.vue',
          'src/views/system/dict/index.vue',
          'src/views/super/online/cgform/index.vue',
          'src/views/super/online/cgform/auto/default/OnlineAutoList.vue',
          'src/views/super/airag/aiapp/AiAppList.vue',
        ],
      },
      // update-end--author:liaozhiyang---date:20260306---for:【QQYUN-14801】Khi vite khởi động, tiền xây dựng một số trang entry để truy cập nhanh hơn
    },
    build: {
      // Vite 8 mặc định dùng Oxc minifier; 'esbuild' đã deprecated.
      // Ở đây giữ minify=true để bật tường minh, đồng thời drop console/debugger.
      minify: true,
      target: 'es2015',
      cssTarget: 'chrome80',
      outDir: OUTPUT_DIR,
      rollupOptions: {
        output: {
          chunkFileNames: 'js/[name]-[hash].js',
          entryFileNames: 'js/[name]-[hash].js',
          // update-begin--author:copilot---date:20260711---for:【Nâng cấp vite8】rolldown không còn hỗ trợ manualChunks dạng đối tượng, đổi sang dạng hàm
          // Cách viết chính thức khuyến nghị: id.includes('node_modules') + split('node_modules/') để lấy tên gói, rồi ánh xạ sang tên chunk (các gói dependency sắp xếp từ lớn đến nhỏ)
          manualChunks(id: string) {
            const normalizedId = id.replace(/\\/g, '/');
            // tinymceGlobalShim phải được đóng gói chung một chunk với lõi/plugin tinymce, đảm bảo window.tinymce được mount trước khi code cấp cao nhất của các plugin/theme thực thi
            if (normalizedId.includes('/src/utils/tinymceGlobalShim')) {
              return 'tinymce-vendor';
            }
            if (!normalizedId.includes('node_modules/')) {
              return;
            }
            const segments = normalizedId.split('node_modules/').pop()!.split('/');
            const pkgName = segments[0].startsWith('@') ? `${segments[0]}/${segments[1]}` : segments[0];
            const packageToChunk: Record<string, string> = {
              vue: 'vue-vendor',
              'vue-router': 'vue-vendor',
              'emoji-mart-vue-fast': 'emoji-mart-vue-fast',
              html2canvas: 'html2canvas-vendor',
              'pinyin-pro': 'pinyin-pro-vendor',
              'vue-grid-layout': 'grid-layout-vendor',
              vuedraggable: 'vuedraggable-vendor',
              tinymce: 'tinymce-vendor',
              '@tinymce/tinymce-vue': 'tinymce-vendor',
            };
            return packageToChunk[pkgName];
          },
          // update-end--author:copilot---date:20260711---for:【Nâng cấp vite8】rolldown không còn hỗ trợ manualChunks dạng đối tượng, đổi sang dạng hàm
          // Vite 8 dùng Oxc thay cho esbuild; esbuild.drop gốc được chuyển sang đây.
          // Xem chi tiết tại https://oxc.rs/docs/guide/usage/minifier/dead-code-elimination
          minify: isBuild ? { compress: { drop: ['console', 'debugger'] } } : false,
        },
      },
      // Tắt hiển thị brotliSize có thể giảm nhẹ thời gian đóng gói
      reportCompressedSize: false,
      // Tăng ngưỡng cảnh báo kích thước tài nguyên tĩnh siêu lớn
      chunkSizeWarningLimit: 2000,
    },
    define: {
      // setting vue-i18-next
      // Suppress warning
      __INTLIFY_PROD_DEVTOOLS__: false,
      __APP_INFO__: JSON.stringify(__APP_INFO__),
    },
    css: {
      // Khi warmup có độ đồng thời cao, việc giao tiếp giữa Less worker và luồng chính có thể vượt quá thời gian timeout nội bộ của Vite
      preprocessorMaxWorkers: 0,
      preprocessorOptions: {
        less: {
          modifyVars: generateModifyVars(),
          javascriptEnabled: true,
        },
      },
    },

    // The vite plugin used by the project. The quantity is large, so it is separately extracted and managed
    plugins: await createVitePlugins(viteEnv, isBuild, isQiankunMicro),

    optimizeDeps: {
      // Vite 8 uses Rolldown for dep optimization; esbuildOptions is deprecated.
      // See https://rolldown.rs/ for the rolldownOptions shape.
      rolldownOptions: {
        transform: {
          target: 'es2020',
        },
      },
      // @iconify/iconify: The dependency is dynamically and virtually loaded by @purge-icons/generated, so it needs to be specified explicitly
      // vite8 tiền xây dựng tường minh các dependency nặng riêng của từng route, tránh việc khi mở route tương ứng lần đầu, do chỉ phát hiện dependency lúc runtime nên phát sinh nhiều request dạng thác nước
      include: [
        'clipboard',
        '@vue/shared',
        '@iconify/iconify',
        'ant-design-vue/es/locale/zh_CN',
        'ant-design-vue/es/locale/en_US',
        // Vite mặc định không quét các mjs đã đóng gói trong node_modules, khiến ant-design-vue/es/vc-picker/generate/dayjs.js
        // Các đường dẫn con của plugin dayjs được import (UMD/CJS) không được tiền đóng gói, lúc runtime báo lỗi "does not provide an export named 'default'".
        // Liệt kê tường minh toàn bộ plugin dayjs mà vc-picker dùng, ép esbuild tiền đóng gói thành ESM.
        'dayjs/plugin/advancedFormat',
        'dayjs/plugin/customParseFormat',
        'dayjs/plugin/weekday',
        'dayjs/plugin/localeData',
        'dayjs/plugin/weekOfYear',
        'dayjs/plugin/weekYear',
        'dayjs/plugin/quarterOfYear',
        'tinymce',
        '@tinymce/tinymce-vue',
        'echarts',
        'vxe-table',
        'vxe-pc-ui',
        'vxe-table-plugin-antd',
      ],
      exclude: [
        // Sau khi nâng cấp lên vite4, cần loại trừ các dependency online và aiflow
        '@jeecg/aiflow',
      ],
    },
  };
};
