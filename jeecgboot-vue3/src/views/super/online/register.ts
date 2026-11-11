import type {App} from 'vue';
import {createAsyncComponent} from "@/utils/factory/createAsyncComponent";
import {registerCgformRouter} from "./cgform/router/cgformRouter";
import {registerCgreportRouter} from "./cgreport/router/cgreportRouter";
import {registerGraphreportRouter} from "./graphreport/router/graphreportRouter";

/** Đăng ký online */
export async function register(app: App) {

  // Đăng kýOnlinePopup
  const OnlineAutoModalAsync = createAsyncComponent(() => import('./cgform/auto/default/OnlineAutoModal.vue'), {loading: true});
  app.component('OnlineAutoModal', OnlineAutoModalAsync);
  // update-begin--author:liusq---date:20261102---for：【onlineBình luận biểu mẫu】Thông báo hệ thống xem chi tiết đổi sang dùng popup chi tiết phiên bản mới
  // Đăng kýOnlinePopup chi tiết
  const OnlineDetailModalAsync = createAsyncComponent(() => import('./cgform/auto/default/OnlineDetailModal.vue'), {loading: true});
  app.component('OnlineDetailModal', OnlineDetailModalAsync);
  // update-end--author:liusq---date:20261102---for：【onlineBình luận biểu mẫu】Thông báo hệ thống xem chi tiết đổi sang dùng popup chi tiết phiên bản mới

  // Đăng kýOnlineRoute biểu mẫu
  registerCgformRouter();
  // Đăng kýOnlineRoute báo cáo
  registerCgreportRouter();
  // Đăng kýOnlineĐịnh tuyến biểu đồ
  registerGraphreportRouter();

  // Đăng ký trang liên kết ngoài (nếu có)
  await registerCgformShareView();

  console.log('[online] Đăng ký hoàn tất!');
}

// Đăng ký trang liên kết ngoài, tự động phán đoán có tồn tại hay không
async function registerCgformShareView() {
  try {
    const globModels = import.meta.glob('./cgform/share/index.ts');
    if (!globModels) {
      return
    }
    const models = Object.values(globModels);
    if (models.length == 0) {
      return
    }
    const shareModel = await models[0]() as Recordable;
    if (typeof shareModel?.register !== 'function') {
      return
    }
    await shareModel.register();

    console.debug('[online] OnlineĐăng ký route liên kết ngoài của biểu mẫu hoàn tất!');
  } catch (e) {
  }
}
