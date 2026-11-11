// import { router } from "@/router";
// import { apps } from './apps';
//
// export const {registerQiankunRouter} = (function () {
//
//   let registered = false;
//
//   /**
//    * Đăng kýqiankunRoute
//    */
//   function registerQiankunRouter() {
//     if (!router) {
//       // Nếu đối tượng route không tồn tại, gọi đệ quy cho đến khi đối tượng route khả dụng
//       setTimeout(() => registerQiankunRouter(), 1);
//     } else {
//       registerQiankunRouterNow();
//     }
//   }
//
//   function registerQiankunRouterNow() {
//     if (registered) {
//       return;
//     }
//     registered = true;
//     const checkQiankunRoute = (path: string) => apps.some(app => path.startsWith('/' + app.name));
//     // Thêm route guard
//     // Route guard, phán đoán có phải làqiankunRoute micro app
//     router.beforeEach(async (to, from, next) => {
//       const isQiankunRoute = checkQiankunRoute(to.path);
//       if (isQiankunRoute) {
//         // Nếu làqiankunRoute micro app, cài đặtmetaThuộc tính
//         to.meta.isQiankunRoute = true;
//       } else {
//         // Nếu không phảiqiankunRoute micro app, xóametaThuộc tính
//         delete to.meta.isQiankunRoute;
//       }
//       next();
//     });
//   }
//
//
//   return {
//     registerQiankunRouter,
//   }
// })();
