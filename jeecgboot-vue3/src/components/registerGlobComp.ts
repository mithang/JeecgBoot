import type { App } from 'vue';
import { Icon } from './Icon';
import AIcon from '/@/components/jeecg/AIcon.vue';
//TinymceVăn bản phong phú
//  import Editor from '/@/components/Tinymce/src/Editor.vue'

import { Button, JUploadButton } from './Button';
import { Space } from 'ant-design-vue';

// ant-design-vue Thông qua component unplugin-vue-components + AntDesignVueResolver Tự động import theo nhu cầu, không cần đăng ký thủ công
const compList = [Icon, AIcon, JUploadButton];
//Đăng ký component toàn cục của module online
import { registerOnlineComp } from '/@/views/super/online/cgform/auto/comp/index';
import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';


export function registerGlobComp(app: App) {
  compList.forEach((comp) => {
    app.component(comp.name || comp.displayName, comp);
  });
  // Space.Compact Là component con, AntDesignVueResolver không thể tự động phân giải, cần đăng ký thủ công
  app.component('ASpaceCompact', Space.Compact);
  
  //Dashboard phụ thuộc vào Tinymce, cần tải trước (không còn cách nào tải theo nhu cầu nữa)
  // app.component(Editor.name, Editor);
  // Giải thích logic mã: TinymceTải không đồng bộ
  // update-begin--author:liaozhiyang---date:20260227---for:【QQYUN-14751]tinymce rich text, JEasyCron, JLinkTableCard tải bất đồng bộ
  app.component(
    'Tinymce',
    createAsyncComponent(() => import('./Tinymce/src/Editor.vue'), {
      loading: true,
    })
  );
  // update-end--author:liaozhiyang---date:20260227---for:【QQYUN-14751]tinymce rich text, JEasyCron, JLinkTableCard tải bất đồng bộ
  app
    .use(registerOnlineComp)
    .use(Button)
  console.log("---Khởi tạo---, đăng ký toàn cục các component Antd, Dashboard, trình thiết kế quy trình, online, quy trình, v.v.--------------")
}
