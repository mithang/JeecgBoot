import { genMessage } from '../helper';
import antdLocale from 'ant-design-vue/es/locale/vi_VN';

// Gói ngôn ngữ tiếng Việt (vi_VN) — Phase 4 Bước 1.
// Dùng lại toàn bộ module zh-CN vì các giá trị trong đó đã được dịch sang
// tiếng Việt ở Phase 1 (giữ nguyên key i18n); chỉ thay locale Ant Design
// thành vi_VN để các component Ant Design hiển thị tiếng Việt.
const modules = import.meta.glob('./zh-CN/**/*.ts', { eager: true });
export default {
  message: {
    ...genMessage(modules as Recordable<Recordable>, 'zh-CN'),
    antdLocale,
  },
};
