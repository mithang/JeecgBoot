import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
// update-begin--author:liaozhiyang---date:20260318---for:【QQYUN-14948]Cần tương thích để vxetable dù được import vào trang cũng không báo lỗi
// Tương thích khi trang import trực tiếp: bọc bằng component bất đồng bộ để đảm bảo vxe-table Render sau khi đăng ký để tránh trang bị kẹt
export const JVxeTable = createAsyncComponent(
  async () => {
    const app = window['JAppRootInstance'];
    if (app && !app._context.components['VxeTable']) {
      const { registerJVxeTable } = await import('./src/install');
      await registerJVxeTable(app);
      const { registerJVxeCustom } = await import('/@/components/JVxeCustom');
      await registerJVxeCustom();
    }
    const m = await import('./src/JVxeTable');
    return m.default;
  },
  { loading: true }
);
// update-end--author:liaozhiyang---date:20260318---for:【QQYUN-14948]Cần tương thích để vxetable dù được import vào trang cũng không báo lỗi
export { registerJVxeTable } from './src/install';
export { deleteComponent } from './src/componentMap';
export { registerComponent, registerAsyncComponent, registerASyncComponentReal } from './src/utils/registerUtils';
