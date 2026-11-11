// /**
//  * qiankunCấu hình
//  */
// import {
//   start,
//   registerMicroApps,
//   runAfterFirstMounted,
//   addGlobalUncaughtErrorHandler
// } from 'qiankun';
// import { apps, containerId } from './apps';
// import { getProps, initGlState } from './state';
// import { registerQiankunRouter } from './route';
//
// registerQiankunRouter();
//
// /**
//  * Tái cấu trúcapps
//  */
// function filterApps() {
//   apps.forEach((item) => {
//     //Dữ liệu ứng dụng chính cần truyền cho micro app.
//     // @ts-ignore
//     item.props = getProps();
//     //Quy tắc route do micro app kích hoạt
//     // @ts-ignore
//     item.activeRule = genActiveRule('/' + item.activeRule);
//   });
//   return apps;
// }
//
// /**
//  * Lắng nghe route
//  * @param {*} routerPrefix Tiền tố
//  */
// function genActiveRule(routerPrefix) {
//   return (location) => location.pathname.startsWith(routerPrefix);
// }
//
// let retryCount = 0;
//
// /**
//  * Đăng ký micro app
//  */
// function registerApps() {
//   const container = document.querySelector('#' + containerId);
//   if (!container) {
//     // Nếu container không tồn tại, thử đệ quy đăng ký ứng dụng, tối đa thử10lần, mỗi lần cách nhau500Mili giây
//     if (retryCount < 10) {
//       retryCount++;
//       setTimeout(() => registerApps(), 500);
//     }
//   } else {
//     registerAppsNow();
//   }
// }
//
// registerApps['containerId'] = containerId;
//
// function registerAppsNow() {
//   if (window.qiankunStarted) {
//     return;
//   }
//   window.qiankunStarted = true;
//   const _apps = filterApps();
//   // @ts-ignore
//   registerMicroApps(_apps, {
//     beforeLoad: [
//       // @ts-ignore
//       (loadApp) => {
//         console.log('[qiankun] before load', loadApp);
//       },
//     ],
//     beforeMount: [
//       // @ts-ignore
//       (mountApp) => {
//         console.log('[qiankun] before mount', mountApp);
//       },
//     ],
//     afterMount: [
//       // @ts-ignore
//       (mountApp) => {
//         console.log('[qiankun] after mount', mountApp);
//       },
//     ],
//     beforeUnmount: [
//       // @ts-ignore
//       (unloadApp) => {
//         console.log('[qiankun] before unmount', unloadApp);
//       },
//     ],
//     afterUnmount: [
//       // @ts-ignore
//       (unloadApp) => {
//         console.log('[qiankun] after unmount', unloadApp);
//       },
//     ],
//   });
//   // Cài đặt sub app mặc định,và genActiveRuletham số trong giữ nhất quán
//   // setDefaultMountApp();
//   // Micro app đầu tiên mount sau cần gọi phương thức, ví dụ bật một số script giám sát hoặc tracking.
//   runAfterFirstMounted(() => console.log('Bật giám sát'));
//   // Thêm bộ xử lý ngoại lệ chưa bắt được toàn cục.
//   addGlobalUncaughtErrorHandler((event) => console.log(event));
//   // Định nghĩa trạng thái toàn cục
//   initGlState();
//   //Khởi độngqiankun
//   start({});
// }
//
// export default registerApps;
