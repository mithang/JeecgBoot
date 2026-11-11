import { ref, nextTick, getCurrentInstance, watch } from 'vue';
import { getToken } from '/@/utils/auth';
import md5 from 'crypto-js/md5';
import { connectWebSocket, onWebSocket } from '/@/hooks/web/useWebSocket';
import { useGlobSetting } from '/@/hooks/setting';
import { useModal } from '/@/components/Modal';
import { useUserStore } from '/@/store/modules/user';
import { isUrl } from '@/utils/is';
import { getQueryVariable, getUrlParams } from '@/utils';
import { useRouter } from 'vue-router';
import { useMessage } from '@/hooks/web/useMessage';
const { createMessage } = useMessage();
export function useDragNotice() {
  //*********************************websocketCấu hìnhbegin******************************************
  const glob = useGlobSetting();
  const { push, currentRoute } = useRouter();
  const userStore = useUserStore();
  const instance: any = getCurrentInstance();
  // Khởi tạo WebSocket
  function initWebSocket() {
    const token = getToken();
    //Đưa đăng nhậptokenTạo một định danh ngắn
    const wsClientId = md5(token);
    // WebSocketkhác với giao thức dùng cho yêu cầu thông thường,wsTương đương vớihttp, wssTương đương vớihttps
    const url = glob.domainUrl?.replace('https://', 'wss://').replace('http://', 'ws://') + '/dragChannelSocket/' + wsClientId;
    connectWebSocket(url);
    onWebSocket(onWebSocketMessage);
  }

  async function onWebSocketMessage(data) {
    console.log('Bảng điều khiển lắng nghe sự kiện nhấp nútwebsocket', data);
    if (data?.CMD === 'drag') {
      //Hành động kích hoạt: url: Đường dẫn modal: Popup
      const action = data.result.action;
      //Loại popup: Nhấp nút mở popup nào, theotypeMở popup khác nhau
      const type = data.result.type;
      //urlĐịa chỉ, có thể là route, cũng có thể là liên kết ngoài
      let url = data.result.url;
      //Tham số popup hoặcurlTham số
      const record = data.result.records || {};
      console.log('Loại sự kiện nhấp của bảng điều khiểntype', type);
      console.log('Hành động sự kiện nhấp của bảng điều khiểnaction', action);
      console.log('Đường dẫn sự kiện nhấp của bảng điều khiểnurl', url);
      console.log('Tham số sự kiện nhấp của bảng điều khiển', record);
      //1.Nếu là đường dẫn, phán đoán liên kết ngoài hay chuyển route nội bộ
      if (action == 'url') {
        if (record?.type && record?.type === 'file') {
          const fileUrl = getFileAccessHttpUrl(record.fileUrl);
          const fileName = record.fileUrl?.split('/')?.pop()?.split('?')?.[0] || record.title;
          downloadFile(fileUrl, fileName);
          return;
        }
        if (record?.type && record.type === 'url') {
          window.open(record.linkUrl, '_blank');
          return;
        }
        //Xử lý đặc biệt khi tải xuống thường dùng
        if (url == 'fileUrl') {
          url = record[url];
        }
        const urlParamsObj = getUrlParams(url);
        if (url.startsWith('http')) {
          window.open(url, '_blank');
        } else {
          push({ path: urlParamsObj.url, query: { ...urlParamsObj.params, ...record } });
        }
      } else {
        //2.Mở component dự án bằng popup
        switch (type) {
          case 'email':
            //Popup xem hộp thư
            handleOpenType('email', { record });
            break;
          default:
            break;
        }
      }
    }
  }
  //*********************************websocketCấu hìnhend******************************************

  //*********************************Mở popup để sửa, cài đặt động popupbegin*******************************
  //Popup biểu mẫu hiện tại
  const currentModal = ref<string | null>(null);
  //Tham số biểu mẫu hiện tại
  const modalParams = ref<Recordable>({});
  //Cache đăng ký biểu mẫu
  const modalRegCache = ref<Recordable>({});
  //Tham số liên kết component
  const bindParams = ref<Recordable>({});
  /**
   * Theo loại mở popup khác nhau
   * @param type
   * @param params
   */
  async function handleOpenType(type, params) {
    currentModal.value = null;
    modalParams.value = { ...params };
    switch (type) {
      case 'email':
        //Xem email
        currentModal.value = 'EoaMailBoxInModal';
        break;
      default:
        currentModal.value = null;
        break;
    }
    //Đăng ký popup biểu mẫu
    initModalRegister();
    await nextTick(() => {
      if (modalRegCache.value[currentModal.value!]?.isRegister) {
        console.log('Đã đăng ký, đi theo cache');
        modalRegCache.value[currentModal.value!].modalMethods.openModal(true, modalParams.value);
      }
    });
  }
  /**
   * Đăng ký popup khởi tạo
   */
  function initModalRegister() {
    //Nếu biểu mẫu đang chọn lànull, thì không xử lý
    if (!currentModal.value) {
      return;
    }
    //Kiểm tra trong cache có tồn tại không, không tồn tại thì đi theo logic cache
    if (!modalRegCache.value[currentModal.value]) {
      const [registerModal, modalMethods] = useModal();
      modalRegCache.value[currentModal.value] = {
        isRegister: false,
        register: bindRegisterModal(registerModal, modalMethods),
        modalMethods,
      };
    }
  }

  /**
   * Popup đăng ký liên kết
   * @param regFn
   * @param modalMethod
   */
  function bindRegisterModal(regFn, modalMethod) {
    return async (...args) => {
      console.log('Bắt đầu đăng ký:', currentModal.value);
      await regFn(...args);
      console.log('Đăng ký xong:', currentModal.value);
      //Mở popup
      modalMethod.openModal(true, modalParams.value);
      //Cài đặt định danh cache
      modalRegCache.value[currentModal.value!].isRegister = true;
    };
  }
  //*********************************Mở popup để sửa, cài đặt động popupend******************************************
  //Làm mới trang
  function reloadPage() {
    const iframes: any = document.getElementsByClassName('jeecg-iframe-page__main');
    // Sẽ HTMLCollection Chuyển thành mảng
    const iframeArray = Array.from(iframes);
    if (currentRoute.value?.meta?.frameSrc && isDragFrameSrc(currentRoute.value.meta.frameSrc)) {
      const targetIframe: any = iframeArray.find((iframe: any) => iframe.src == currentRoute.value?.meta?.frameSrc);
      console.log('targetIframe', targetIframe);
      if (targetIframe) {
        targetIframe.contentWindow.postMessage({ reload: true }, '*');
      }
    }
  }
  /**
   * Kiểm tra frameSrc Có phải trang bảng điều khiển không
   * Hỗ trợ hai loại URL Hình thức:
   *   1. /drag/view?pageId=xxx(query Hình thức, quy ước phiên bản cũ)
   *   2. /drag/view/xxx(path Hình thức, quy ước phiên bản mới)
   */
  function isDragFrameSrc(frameSrc: string): boolean {
    return /\/(drag|jimubi)\/view(\?|\/|$)/.test(frameSrc || '');
  }
  // update-end--author:liusq---date:20260804---for:[Dashboard] Từ tin nhắn của tôi chuyển về dashboard tab thì bố cục thay đổi, kiểm tra gốc chỉ khớp /drag/view?pageId= Hình thức, chưa bao phủ /drag/view/:id path Hình thức, gây ra iframe Bảng điều khiển bên trong chưa làm mới
  return {
    initDragWebSocket: initWebSocket,
    handleOpenType,
    currentModal,
    modalParams,
    modalRegCache,
    bindParams,
    reloadPage,
  };
}
