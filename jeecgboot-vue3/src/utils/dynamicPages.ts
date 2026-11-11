// Lấy tất cả trang động(viewsTất cả dưới thư mụcvueTệp vàtsxTệp)
const allFiles = import.meta.glob(
  [
    '../views/**/*.{vue,tsx}',// Lấy tất cảvuevàtsxTệp
    // Loại trừ thư mục cụ thể
    '!../views/system/approvalrole/compoments/**',
  ]
);
// Hợp nhất tất cả trang động
export const dynamicPages = { ...allFiles };
