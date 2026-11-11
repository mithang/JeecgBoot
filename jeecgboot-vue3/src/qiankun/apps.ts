// export const containerId = 'qiankun-content'
//
// /**
//  *Micro appapps
//  * @name: Tên micro app - Có tính duy nhất
//  * @entry: Điểm vào micro app.Bắt buộc - Tải micro app qua địa chỉ đó,
//  * @container: Nút mount micro app - Sau khi micro app tải xong sẽ mount vào nút này
//  * @activeRule: Quy tắc route do micro app kích hoạt - Sau khi kích hoạt quy tắc route sẽ tải micro app này
//  */
// //Danh sách micro app
// const _apps: Recordable[] = [];
// for (const key in import.meta.env) {
//   if (key.includes('VITE_APP_SUB_')) {
//     const name = key.split('VITE_APP_SUB_')[1];
//     const obj = {
//       name,
//       entry: import.meta.env[key],
//       container: '#' + containerId,
//       activeRule: name,
//     };
//     _apps.push(obj);
//   }
// }
// export const apps = _apps;
