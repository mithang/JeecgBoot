/*
 *
 * Ở đây điền biểu thức tùy chỉnh của người dùng
 * Có thể dùng trongOnlineDùng trong biểu thức giá trị mặc định của biểu mẫu
 * Biến hoặc phương thức cần dùng bên ngoài nhất định phải export, nếu không không thể nhận biết
 * Ví dụ:
 *   export const name = 'Trương Tam'; // const là hằng số
 *   export let age = 17; // Tùy tình huống export const vẫn là let , cả hai đều có thể dùng bình thường
 *   export function content(arg) { // export Phương thức, có thể truyền tham số, khi dùng phải thêm dấu ngoặc, giá trị nhất định phảireturnQuay lại, có thể trả vềPromise
 *     return 'content' + arg;
 *   }
 *   export const address = (arg) => content(arg) + ' | Bắc Kinh'; // export Hàm mũi tên cũng có thể
 *
 */

/** Ví dụ chính thức giá trị mặc định của trường: lấy địa chỉ */
export function demoFieldDefVal_getAddress(arg) {
  if (!arg) {
    arg = 'Quận Triều Dương';
  }
  return `Bắc Kinh ${arg}`;
}

/** Tùy chỉnhJSVí dụ hàm */
export function sayHi(name) {
  if (!name) {
    name = 'Trương Tam';
  }
  return `Xin chào, tôi tên là: ${name}`;
}
