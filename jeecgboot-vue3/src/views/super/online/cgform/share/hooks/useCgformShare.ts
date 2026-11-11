import {ref} from 'vue';
import {router} from "@/router";

import {
  SHARE_ADD_ROUTER_NAME,
  SHARE_DETAIL_ROUTER_NAME,
  SHARE_LOGIN__ROUTER_NAME,
  SHARE_UPDATE_ROUTER_NAME
} from "../route";
import {useShareStore} from "../store/shareStore";
import {useUserStore} from "@/store/modules/user";
import {getCgformById, getCgformRecordById} from "../share.api";
import {parseExtConfigJson} from "../../util/utils";

export function useCgformShare() {
  const userStore = useUserStore()
  const shareStore = useShareStore()

  const pageLoading = ref(true)
  const pageErrorTip = ref('')

  async function initCgformShare() {
    try {
      // Kiểm traurltrong đótoken
      await shareStore.checkUrlToken()
      const route = router.currentRoute.value
      // Kiểm tra trong bộ nhớ đệmtoken
      if (!userStore.getToken) {
        // Chuyển tới trang đăng nhập
        router.push({
          name: SHARE_LOGIN__ROUTER_NAME,
          query: {
            redirect: encodeURIComponent(route.path),
          }
        });
        return
      }
      // Lấy OnlineBiểu mẫu thông tin của
      const {id: formId} = route.params
      if (!formId) {
        pageErrorTip.value = 'Lỗi tham số'
        return
      }
      let res = await getCgformById(formId as string);
      if (!res.success) {
        pageErrorTip.value = res.message
        return
      }
      const record = res.result
      // Phán đoán có bật liên kết ngoài hay không
      const extJson = parseExtConfigJson(record);
      if (!extJson?.enableExternalLink) {
        pageErrorTip.value = 'Biểu mẫu hiện tại chưa bật liên kết ngoài'
        return
      }
      // Phán đoán có hỗ trợ thao tác hiện tại hay không
      let externalLinkActions = extJson.externalLinkActions.split(',');
      if (route.name === SHARE_ADD_ROUTER_NAME) {
        if (!externalLinkActions.includes('add')) {
          pageErrorTip.value = 'Biểu mẫu hiện tại không hỗ trợ thêm mới từ bên ngoài'
          return
        }
      } else if (route.name === SHARE_UPDATE_ROUTER_NAME) {
        if (!externalLinkActions.includes('edit')) {
          pageErrorTip.value = 'Biểu mẫu hiện tại không hỗ trợ chỉnh sửa từ bên ngoài'
          return
        }
      } else if (route.name === SHARE_DETAIL_ROUTER_NAME) {
        if (!externalLinkActions.includes('detail')) {
          pageErrorTip.value = 'Biểu mẫu hiện tại không hỗ trợ xem chi tiết từ bên ngoài'
          return
        }
      } else {
        pageErrorTip.value = 'Trang không xác định';
        return;
      }

      // Phán đoán loại biểu mẫu
      if (record.tableType == 3) {
        pageErrorTip.value = 'không hỗ trợ liên kết ngoài của bảng phụ';
        return;
      }

      shareStore.setCgformRecord(record);

      // Truy vấn dữ liệu
      if (route.name === SHARE_UPDATE_ROUTER_NAME || route.name === SHARE_DETAIL_ROUTER_NAME) {
        const {dataId} = route.params;
        if (!dataId) {
          pageErrorTip.value = 'Lỗi tham số'
          return
        }
        res = await getCgformRecordById(formId as string, dataId as string);
        if (!res.success) {
          pageErrorTip.value = res.message
          return
        }
        const dataRecord = res.result
        if (dataRecord?.id !== dataId) {
          pageErrorTip.value = 'Dữ liệu không tồn tại hoặc đã bị xóa'
          return
        }
        shareStore.setDataRecord(dataRecord);
      }

    } catch (e: any) {
      pageErrorTip.value = e?.message || e
      console.error(e)
    } finally {
      pageLoading.value = false
    }
  }

  return {
    pageLoading,
    pageErrorTip,

    initCgformShare,
  }
}