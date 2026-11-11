// update-begin--author:copilot---date:20260711---for:[Kiểm tra nâng cấp dependency] Sửa Rolldown Nghiêm ngặt CJS Tương tác gây ra tinymce Toàn cục chưa được mount
// tinymce Gói lõi(tinymce/tinymce)là UMD Cách viết:`typeof module === 'object' && module.exports ? module.exports = tinymce : window.tinymce = tinymce`.
// Rolldown Đối với CJS tương tác của Rollup/esbuild Nghiêm ngặt hơn, sẽ cung cấp cho tệp này module Đối tượng,
// Khiến nó luôn đi module.exports Nhánh, không kèm theo window.tinymce Cài đặt xong.
// mà tinymce các plugin/theme Gói (ví dụ tinymce/plugins/fullscreen) tham chiếu trực tiếp biến trần ở tầng trên cùng của module `tinymce`
// (Ví dụ `tinymce.util.Tools.resolve('tinymce.PluginManager')`), phụ thuộc window.tinymce Đã mount trước,
// Nếu không sẽ ném ra `ReferenceError: tinymce is not defined`.
// Ở đây thống nhất import từ tệp này tinymce Lõi, đảm bảo trong phần còn lại tinymce Module con (themes/plugins/icons) bị import Trước đây,
// window.tinymce Đã mount rõ ràng.
import tinymceCore from 'tinymce/tinymce';

if (typeof window !== 'undefined') {
  (window as any).tinymce = tinymceCore;
}

export default tinymceCore;
// update-end--author:copilot---date:20260711---for:[Kiểm tra nâng cấp dependency] Sửa Rolldown Nghiêm ngặt CJS Tương tác gây ra tinymce Toàn cục chưa được mount
