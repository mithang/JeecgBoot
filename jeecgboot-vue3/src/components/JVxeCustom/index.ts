import { registerComponent, registerAsyncComponent, registerASyncComponentReal } from '/@/components/jeecg/JVxeTable';
import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
import { DictSearchSpanCell, DictSearchInputCell } from './src/components/JVxeSelectDictSearchCell';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
export async function registerJVxeCustom() {
  // ----------------- ⚠ Lưu ý ⚠ -----------------
  //  Khi trong component có chứa BasicModal, bắt buộc phải dùng import bất đồng bộ!
  //  Nếu không sẽ khiến i18n mất hiệu lực!
  // ----------------- ⚠ Lưu ý ⚠ -----------------

  // Đăng ký [Popup] (cách đóng gói thông thường)
  await registerAsyncComponent(JVxeTypes.popup, import('./src/components/JVxePopupCell.vue'));
  // update-begin--author:liaozhiyang---date:20260507---for：【issues/7617]Bảng con vxetable hỗ trợ danh mục popup
  // Đăng ký [Từ điển Popup]
  await registerAsyncComponent(JVxeTypes.popupDict, import('./src/components/JVxePopupDictCell.vue'));
  // update-end--author:liaozhiyang---date:20260507---for：【issues/7617]Bảng con vxetable hỗ trợ danh mục popup

  // Đăng ký component [Tìm kiếm dropdown theo danh mục] (cách đóng gói nâng cao)
  registerComponent(JVxeTypes.selectDictSearch, DictSearchInputCell, DictSearchSpanCell);

  // Đăng ký component [Tải lên tệp]
  await registerAsyncComponent(JVxeTypes.file, import('./src/components/JVxeFileCell.vue'));
  // Đăng ký component [Tải lên hình ảnh]
  await registerAsyncComponent(JVxeTypes.image, import('./src/components/JVxeImageCell.vue'));
  // Đăng ký component [Chọn người dùng]
  await registerAsyncComponent(JVxeTypes.userSelect, import('./src/components/JVxeUserSelectCell.vue'));
  // Đăng ký component [Chọn phòng ban]
  await registerAsyncComponent(JVxeTypes.departSelect, import('./src/components/JVxeDepartSelectCell.vue'));
  // update-begin--author:liaozhiyang---date:20260317---for:【QQYUN-9441] online một-nhiều thêm bản ghi liên kết và trường bảng khác
  // Đăng ký component [Bản ghi liên kết]
  await registerAsyncComponent(JVxeTypes.linkTable, import('./src/components/JVxeLinkTableCell.vue'));
  // update-end--author:liaozhiyang---date:20260317---for:【QQYUN-9441] online một-nhiều thêm bản ghi liên kết và trường bảng khác
  // Đăng ký component [Chọn tỉnh/thành phố/quận huyện]
  // await registerAsyncComponent(JVxeTypes.pca, import('./src/components/JVxePcaCell.vue'));
  // Giải thích logic mã: 【QQYUN-8241] để tránh tải lần đầuchina-area-data, component JVxePcaCell cần tải bất đồng bộ
  registerASyncComponentReal(
    JVxeTypes.pca,
    createAsyncComponent(() => import('./src/components/JVxePcaCell.vue'))
  );
}
