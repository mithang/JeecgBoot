import type { App } from 'vue';
import { warn } from '/@/utils/log';
import { registerDynamicRouter } from '/@/utils/monorepo/dynamicRouter';

// Cấu hình module tải lười (tải theo nhu cầu, chỉ tải gói tương ứng khi truy cập route liên quan)
const lazyPackages = [
  { name: '@jeecg/aiflow', importer: () => import('@jeecg/aiflow') },
];

let appInstance: App | null = null;

// noinspection JSUnusedGlobalSymbols
const installOptions = {
  baseImport,
};

export function registerPackages(app: App) {
  // Chỉ lưu app Instance, không tải module ngay
  appInstance = app;
  // app.component(
  //   'SuperQuery',
  //   createAsyncComponent(() => import('@jeecg/online').then(mod => mod.SuperQuery))
  // );
  // app.component(
  //   'JOnlineSearchSelect',
  //   createAsyncComponent(() => import('@jeecg/online').then(mod => mod.JOnlineSearchSelect))
  // );
}

/** Cache gói đã tải */
const loadedPackages = new Map<string, any>();
/** Gói đang tải Promise Cache (tránh tải trùng) */
const loadingPromises = new Map<string, Promise<any>>();

/**
 * Tải gói theo nhu cầu và đăng ký
 */
async function ensurePackageLoaded(pkgConfig: typeof lazyPackages[number]) {
  const { name, importer } = pkgConfig;
  if (loadedPackages.has(name)) {
    return loadedPackages.get(name);
  }
  if (!loadingPromises.has(name)) {
    const promise = importer().then((pkg) => {
      const mod = pkg.default || pkg;
      if (appInstance) {
        appInstance.use(mod, installOptions);
        registerDynamicRouter(mod.getViews);
      }
      loadedPackages.set(name, mod);
      loadingPromises.delete(name);
      return mod;
    });
    loadingPromises.set(name, promise);
  }
  return loadingPromises.get(name);
}

/**
 * Theo component Gói được tải ưu tiên khi từ khóa đường dẫn khớp
 */
function getMatchedPackage(component: string): typeof lazyPackages[number] | null {
  const lc = component.toLowerCase();
  for (const pkgConfig of lazyPackages) {
    // Trích từ khóa từ tên gói, ví dụ @jeecg/online -> online, @jeecg/aiflow -> aiflow
    const keyword = pkgConfig.name.split('/').pop()!;
    if (lc.includes(keyword)) {
      return pkgConfig;
    }
  }
  return null;
}

/**
 * Tìm component trong gói đã chỉ định
 */
async function findComponentInPackage(pkgConfig: typeof lazyPackages[number], component: string): Promise<(() => Promise<Recordable>) | null> {
  try {
    const mod = await ensurePackageLoaded(pkgConfig);
    const views = mod.getViews();
    for (const key of Object.keys(views)) {
      const k = key.replace('./src/views', '');
      const startFlag = component.startsWith('/');
      const endFlag = component.endsWith('.vue') || component.endsWith('.tsx');
      const startIndex = startFlag ? 0 : 1;
      const lastIndex = endFlag ? k.length : k.lastIndexOf('.');
      if (k.substring(startIndex, lastIndex) === component) {
        return views[key];
      }
    }
  } catch (e) {
    // Gói không tồn tại hoặc tải thất bại, bỏ qua
  }
  return null;
}

/**
 * Tải component gói theo nhu cầu: gọi khi route không khớp component local
 * Theo component Từ khóa trong đường dẫn ưu tiên khớp gói tương ứng, tránh duyệt vô nghĩa
 */
export async function loadPackageComponent(component: string): Promise<(() => Promise<Recordable>) | null> {
  // Ưu tiên khớp chính xác gói theo từ khóa
  const matched = getMatchedPackage(component);
  if (matched) {
    return findComponentInPackage(matched, component);
  }
  // Chưa khớp từ khóa, thử lần lượt tất cả các gói
  for (const pkgConfig of lazyPackages) {
    const result = await findComponentInPackage(pkgConfig, component);
    if (result) return result;
  }
  return null;
}

// Có thể dùng trong moduleimport
const importGlobs = [import.meta.glob('../../utils/**/*.{ts,js,tsx}'), import.meta.glob('../../hooks/**/*.{ts,js,tsx}')];

/**
 * Import gói dự án cơ sở
 * Hiện tại hỗ trợ import như sau
 * /@/utils/**
 * /@/hooks/**
 *
 * @param path Đường dẫn tệp,tsKhông cần nhập hậu tố. Ví dụ:/@/utils/common/compUtils
 */
async function baseImport(path: string) {
  if (path) {
    // Sẽ /@/ Thay thành ../../
    path = path.replace(/^\/@\//, '../../');
    for (const glob of importGlobs) {
      for (const key of Object.keys(glob)) {
        if (path === key || `${path}.ts` === key || `${path}.tsx` === key) {
          return glob[key]();
        }
      }
    }
    warn(`Import thất bại:${path} Không tồn tại`);
  }
  return null;
}
