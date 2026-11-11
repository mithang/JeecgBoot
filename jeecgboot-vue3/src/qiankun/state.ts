// /**
//  *Dữ liệu chung
//  */
// import { initGlobalState } from 'qiankun';
// import { store } from '/@/store';
// import { router } from '/@/router';
// import { getToken } from '/@/utils/auth';
// //Định nghĩa dữ liệu truyền vào micro app
// export function getProps() {
//   return {
//     data: {
//       publicPath: '/',
//       token: getToken(),
//       store,
//       router,
//     },
//   };
// }
//
// /**
//  * Định nghĩa trạng thái toàn cục, và trả về phương thức giao tiếp,Dùng trong ứng dụng chính, micro app thông qua props Lấy phương thức giao tiếp.
//  * @param state Dữ liệu chung ứng dụng chính truyền sang
//  */
// export function initGlState(info = { userName: 'admin' }) {
//   // Khởi tạostate
//   const actions = initGlobalState(info);
//   // Cài đặt giá trị mới
//   actions.setGlobalState(info);
//   // Đăng ký Observer Hàm - Phản hồi globalState Thay đổi, trong globalState Khi xảy ra thay đổi thì kích hoạt Observer Hàm.
//   actions.onGlobalStateChange((newState, prev) => {
//     // state: Trạng thái sau khi thay đổi; prev Trạng thái trước khi thay đổi
//     console.info('newState', newState);
//     console.info('prev', prev);
//     for (const key in newState) {
//       console.info('onGlobalStateChange', key);
//     }
//   });
// }
