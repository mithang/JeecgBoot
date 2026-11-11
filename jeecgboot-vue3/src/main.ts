import type { MainAppProps } from "#/main";
import 'uno.css';
import '/@/design/index.less';
import 'ant-design-vue/dist/reset.css';
// Đăng ký biểu tượng
import 'virtual:svg-icons-register';

import App from './App.vue';
import { createApp } from 'vue';
import { initAppConfigStore } from '/@/logics/initAppConfig';
import { setupErrorHandle } from '/@/logics/error-handle';
import { router, createRouter, setupRouter } from '/@/router';
import { setupRouterGuard } from '/@/router/guard';
import { setupStore } from '/@/store';
import { setupGlobDirectives } from '/@/directives';
import { setupI18n } from '/@/locales/setupI18n';
import { setupElectron } from "@/electron";
import { registerGlobComp } from '/@/components/registerGlobComp';
import { registerThirdComp } from '/@/settings/registerThirdComp';
import { registerSuper } from '/@/views/super/registerSuper';
import { useSso } from '/@/hooks/web/useSso';
import { checkIsQiankunMicro } from "/@/qiankun/micro";
import { autoUseQiankunMicro } from "/@/qiankun/micro/qiankunMicro";
import { useAppStoreWithOut } from "@/store/modules/app";

// Đăng kýonlineModulelib
import { registerPackages } from '/@/utils/monorepo/registerPackages';

// Điểm vào chương trình
async function main() {
  if (checkIsQiankunMicro()) {
    // 【JEECGLà micro app Qiankun] Khởi động ở chế độ micro app Qiankun
    // await autoUseQiankunMicro(bootstrap)
    await autoUseQiankunMicro(bootstrap)
  } else {
    // Lấy tham số
    const props = getMainAppProps();
    // Khởi động thông thường
    await bootstrap(props)
  }
}

main();

async function bootstrap(props?: MainAppProps) {
  // Tạo instance ứng dụng
  const app = createApp(App);
  // 【QQYUN-6329】
  window['JAppRootInstance'] = app;

  // Tạo route
  createRouter();

  // Cấu hình lưu trữ
  setupStore(app);

  // Cấu hình tham số
  setupProps(props);

  // Cấu hình đa ngôn ngữ,Trường hợp bất đồng bộ:Tệp ngôn ngữ có thể lấy từ phía server
  await setupI18n(app);

  // Khởi tạo cấu hình hệ thống nội bộ
  initAppConfigStore();

  // Đăng ký route module ngoài(Đăng kýonlineModulelib)
  registerPackages(app);

  // Đăng ký component toàn cục
  registerGlobComp(app);

  //CASĐăng nhập một lần
  await useSso().ssoLogin();

  // Đăng kýsuperRoute ứng dụng
  await registerSuper(app);
  
  // Cấu hình route
  setupRouter(app);

  // Bảo vệ route
  setupRouterGuard(router);

  // Đăng ký chỉ thị toàn cục
  setupGlobDirectives(app);

  // Cấu hình xử lý lỗi toàn cục
  setupErrorHandle(app);

  // Đăng ký component bên thứ ba
  await registerThirdComp(app);

  // Cấu hìnhelectron
  setupElectron(app)

  // Khi route đã sẵn sàng mới thực thi mount( https://next.router.vuejs.org/api/#isready)
  await router.isReady();

  // Mount ứng dụng
  app.mount(getMountContainer(props), true);

  console.log(" vue3 app Tải xong!")

  return app
}

// Lấy container mount ứng dụng
function getMountContainer(props?: MainAppProps) {
  const id = '#app';
  if (!props?.container?.querySelector) {
    return id;
  }
  return props.container.querySelector(id) ?? id;
}

// Lấy tham số ứng dụng chính
function getMainAppProps(): MainAppProps {
  // Từ queryString lấy trong
  const searchParams = new URLSearchParams(window.location.search);
  // Ẩn sidebar (menu)
  let hideSider = searchParams.get('hideSider') === 'true';
  // Ẩn phần trên
  let hideHeader = searchParams.get('hideHeader') === 'true';
  // Ẩn NhiềuTab Chuyển đổi
  let hideMultiTabs = searchParams.get('hideMultiTabs') === 'true';

  return {
    hideSider,
    hideHeader,
    hideMultiTabs
  }
}

// Cấu hình tham số ứng dụng chính
function setupProps(props?: MainAppProps) {
  if (!props) {
    return
  }
  const appStore = useAppStoreWithOut();
  appStore.setMainAppProps(props);
}
