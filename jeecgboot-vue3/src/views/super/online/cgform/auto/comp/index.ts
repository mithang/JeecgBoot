import type { App } from 'vue';
import {defineAsyncComponent} from 'vue'
const SuperQuery = defineAsyncComponent(() => import('./superquery/SuperQuery.vue'))
const JOnlineSearchSelect = defineAsyncComponent(() => import('./JOnlineSearchSelect.vue'))

export const registerOnlineComp = {
  install(app: App) {
    app.component('JOnlineSearchSelect', JOnlineSearchSelect);
    app.component('SuperQuery', SuperQuery);

    console.log("---Khởi tạo---， Đăng ký toàn cụcOnlineMột phần component--------------")
  },
};
