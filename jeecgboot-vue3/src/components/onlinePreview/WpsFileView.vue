<!--wpsTrang xem trước tệp-->
<template></template>

<script lang="ts" setup name="online-preview">
  import { router } from '@/router';
  import { defHttp } from '/@/utils/http/axios';
  import OpenSDK from './open-jssdk.es';
  import { getToken } from '@/utils/auth';
  import {useMessage} from "@/hooks/web/useMessage";

  const {createWarningModal} = useMessage()

  initWpsPath();

  /**
   * Khởi tạo đường dẫn wps
   */
  async function initWpsPath() {
    let query = router.currentRoute.value.query;
    if (query && query.url) {
      let result = await defHttp.get(
        { url: '/eoa/wps/oaWpsFile/getOfflineViewWpsUrl/fileurl', params: { fileUrl: query.url } },
        { isTransformResponse: false }
      );
      if (result && result.success) {
        const instance = OpenSDK.config({
          url: result.message,
          mode: 'normal',
          mount: document.querySelector('#app'),
        });
        // Cài đặt token
        instance.setToken({
          token: getToken(),
        });
      } else {
        const msg = 'Yêu cầu địa chỉ xem trước thất bại' + (result.message ? `：${result.message}` : ', vui lòng thử lại!');
        createWarningModal({
          title: 'Nhắc nhở',
          content: msg,
          onOk: () => window.close(),
        });
      }
    } else {
      createWarningModal({
        title: 'Nhắc nhở',
        content: 'Lấy địa chỉ xem trước thất bại, vui lòng thử lại!',
        onOk: () => window.close(),
      });
    }
  }
</script>

<style lang="less">
  #office-iframe {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 9999;
    background: white;
  }
</style>
