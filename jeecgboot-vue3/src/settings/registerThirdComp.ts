import type { App } from 'vue';
import { registerJVxeTable } from '/@/components/jeecg/JVxeTable';
import { registerJVxeCustom } from '/@/components/JVxeCustom';

// Đăng ký dayjs toàn cục
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';

// JVxeTable tải theo nhu cầu: chỉ đăng ký một lần khi render lần đầu
let jvxeRegistered = false;

export async function registerThirdComp(app: App) {
  //---------------------------------------------------------------------
  // update-begin--author:liaozhiyang---date:20260210---for:【QQYUN-13658】Jvxetable, vxetable tải theo nhu cầu
  // Đăng ký component JVxeTable (tải theo nhu cầu: lần đầu dùng <JVxeTable> mới tải vxe-table và JVxeTable)
  app.component(
    'JVxeTable',
    createAsyncComponent(
      () => {
        return import('/@/components/jeecg/JVxeTable/src/JVxeTable').then(async (m) => {
          if (!jvxeRegistered) {
            if (app._context.components.VxeTable) {
              // Đã đăng ký toàn cục
            } else {
              const { registerJVxeTable } = await import('/@/components/jeecg/JVxeTable/src/install');
              await registerJVxeTable(app);
              const { registerJVxeCustom } = await import('/@/components/JVxeCustom');
              await registerJVxeCustom();
              jvxeRegistered = true;
            }
          }
          return m.default;
        });
      },
      { loading: true }
    )
  );
  // update-end--author:liaozhiyang---date:20260209---for:【QQYUN-13658】Jvxetable, vxetable tải theo nhu cầu
  //---------------------------------------------------------------------
  // Đăng ký gói emoji trò chuyện toàn cục
  // Giải thích logic mã: 【QQYUN-8241】thư viện emoji-mart-vue-fast tải bất đồng bộ
  app.component(
    'Picker',
    createAsyncComponent(() => {
      return new Promise((resolve, rejected) => {
        import('emoji-mart-vue-fast/src')
          .then((res) => {
            const { Picker } = res;
            resolve(Picker);
          })
          .catch((err) => {
            rejected(err);
          });
      });
    })
  );
  // update-end--author:liaozhiyang---date:20240308---for:【QQYUN-8241】thư viện emoji-mart-vue-fast tải bất đồng bộ
  //---------------------------------------------------------------------
  // Đăng ký dayjs toàn cục
  dayjs.locale('zh-cn');
  dayjs.extend(relativeTime);
  dayjs.extend(customParseFormat);
  app.config.globalProperties.$dayjs = dayjs
  app.provide('$dayjs', dayjs)
  //---------------------------------------------------------------------
}
