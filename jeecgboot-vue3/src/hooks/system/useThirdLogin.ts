import { ref, unref } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useGlobSetting } from '/@/hooks/setting';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStore } from '/@/store/modules/user';
import { setThirdCaptcha, getCaptcha } from '/@/api/sys/user';
import { useI18n } from '/@/hooks/web/useI18n';

export function useThirdLogin() {
  const { createMessage, notification } = useMessage();
  const { t } = useI18n();
  const glob = useGlobSetting();
  const userStore = useUserStore();
  //Loại bên thứ ba
  const thirdType = ref('');
  //Thông tin liên quan đăng nhập bên thứ ba
  const thirdLoginInfo = ref<any>({});
  //Trạng thái
  const thirdLoginState = ref(false);
  //Popup liên kết số điện thoại
  const bindingPhoneModal = ref(false);
  //Người dùng bên thứ baUUID
  const thirdUserUuid = ref('');
  //Cửa sổ gợi ý
  const thirdConfirmShow = ref(false);
  //Popup mật khẩu liên kết
  const thirdPasswordShow = ref(false);
  //Mật khẩu liên kết
  const thirdLoginPassword = ref('');
  //Liên kết người dùng
  const thirdLoginUser = ref('');
  //Đang tải
  const thirdCreateUserLoding = ref(false);
  //Liên kết số điện thoại
  const thirdPhone = ref('');
  //Mã xác thực
  const thirdCaptcha = ref('');
  //Đăng nhập bên thứ ba
  function onThirdLogin(source) {
    let url = `${glob.uploadUrl}/sys/thirdLogin/render/${source}`;
    const openWin = window.open(
      url,
      `login ${source}`,
      'height=500, width=500, top=0, left=0, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no'
    );
    thirdType.value = source;
    thirdLoginInfo.value = {};
    thirdLoginState.value = false;
    let receiveMessage = function (event) {
      let token = event.data;
      if (typeof token === 'string') {
        //Nếu là kiểu chuỗi Giải thích làtokenThông tin
        if (token === 'Đăng nhập thất bại') {
          createMessage.warning(token);
        } else if (token.includes('Liên kết số điện thoại')) {
          bindingPhoneModal.value = true;
          let strings = token.split(',');
          thirdUserUuid.value = strings[1];
        } else if (token.split('.').length === 3) {
          // JWT Kiểm tra định dạng: phải chứa hai '.', lọc Feishu SDK bên thứ ba v.v. postMessage(Ví dụ [tea-sdk]ready)
          doThirdLogin(token);
        }
      } else if (typeof token === 'object') {
        //Loại đối tượng Giải thích cần nhắc có liên kết tài khoản hiện có không
        if (token['isObj'] === true) {
          thirdConfirmShow.value = true;
          thirdLoginInfo.value = { ...token };
        }
      } else {
        createMessage.warning('Truyền thông tin không nhận dạng được');
      }
      // Giải thích logic mã: 【TV360X-1827】macSau khi đăng nhập bên thứ ba WeCom bằng Chrome hệ thống thành công, không bật popup liên kết điện thoại
      if (openWin?.closed) {
        window.removeEventListener('message', receiveMessage, false);
      }
    };
    // Giải thích logic mã: 【TV360X-1827】macSau khi đăng nhập bên thứ ba WeCom bằng Chrome hệ thống thành công, không bật popup liên kết điện thoại
    window.removeEventListener('message', receiveMessage, false);
    window.addEventListener('message', receiveMessage, false);
  }
  // TheotokenThực hiện đăng nhập
  function doThirdLogin(token) {
    if (unref(thirdLoginState) === false) {
      thirdLoginState.value = true;
      userStore.ThirdLogin({ token, thirdType: unref(thirdType) }).then((res) => {
        console.log('res====>doThirdLogin', res);
        if (res && res.userInfo) {
          notification.success({
            message: t('sys.login.loginSuccessTitle'),
            description: `${t('sys.login.loginSuccessDesc')}: ${res.userInfo.realname}`,
            duration: 3,
          });
        } else {
          requestFailed(res);
        }
      });
    }
  }

  function requestFailed(err) {
    notification.error({
      message: 'Đăng nhập thất bại',
      description: ((err.response || {}).data || {}).message || err.message || 'Yêu cầu gặp lỗi, vui lòng thử lại sau',
      duration: 4,
    });
  }
  // Liên kết tài khoản đã có Cần nhập mật khẩu
  function thirdLoginUserBind() {
    thirdLoginPassword.value = '';
    thirdLoginUser.value = thirdLoginInfo.value.uuid;
    thirdConfirmShow.value = false;
    thirdPasswordShow.value = true;
  }
  //Tạo tài khoản mới
  function thirdLoginUserCreate() {
    thirdCreateUserLoding.value = true;
    // Thêm hai số ngẫu nhiên sau tên tài khoản
    thirdLoginInfo.value.suffix = parseInt(Math.random() * 98 + 1);
    defHttp
      .post({ url: '/sys/third/user/create', params: { thirdLoginInfo: unref(thirdLoginInfo) } }, { isTransformResponse: false })
      .then((res) => {
        if (res.success) {
          let token = res.result;
          doThirdLogin(token);
          thirdConfirmShow.value = false;
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        thirdCreateUserLoding.value = false;
      });
  }
  // Xác minh mật khẩu
  function thirdLoginCheckPassword() {
    let params = Object.assign({}, unref(thirdLoginInfo), { password: unref(thirdLoginPassword) });
    defHttp.post({ url: '/sys/third/user/checkPassword', params }, { isTransformResponse: false }).then((res) => {
      if (res.success) {
        thirdLoginNoPassword();
        doThirdLogin(res.result);
      } else {
        createMessage.warning(res.message);
      }
    });
  }
  // Không có mật khẩu Hủy thao tác
  function thirdLoginNoPassword() {
    thirdPasswordShow.value = false;
    thirdLoginPassword.value = '';
    thirdLoginUser.value = '';
  }

  //Hàm chạy trước khi đếm ngược
  function sendCodeApi() {
    //return setThirdCaptcha({mobile:unref(thirdPhone)});
    return getCaptcha({ mobile: unref(thirdPhone), smsmode: '0' });
  }
  //Nhấp nút xác nhận để liên kết số điện thoại
  function thirdHandleOk() {
    if (!unref(thirdPhone)) {
      cmsFailed('Vui lòng nhập số điện thoại');
    }
    if (!unref(thirdCaptcha)) {
      cmsFailed('Vui lòng nhập mã xác thực');
    }
    let params = {
      mobile: unref(thirdPhone),
      captcha: unref(thirdCaptcha),
      thirdUserUuid: unref(thirdUserUuid),
    };
    defHttp.post({ url: '/sys/thirdLogin/bindingThirdPhone', params }, { isTransformResponse: false }).then((res) => {
      if (res.success) {
        bindingPhoneModal.value = false;
        doThirdLogin(res.result);
      } else {
        createMessage.warning(res.message);
      }
    });
  }
  function cmsFailed(err) {
    notification.error({
      message: 'Đăng nhập thất bại',
      description: err,
      duration: 4,
    });
    return;
  }
  //Trả về dữ liệu và phương thức
  return {
    thirdPasswordShow,
    thirdLoginCheckPassword,
    thirdLoginNoPassword,
    thirdLoginPassword,
    thirdConfirmShow,
    thirdCreateUserLoding,
    thirdLoginUserCreate,
    thirdLoginUserBind,
    bindingPhoneModal,
    thirdHandleOk,
    thirdPhone,
    thirdCaptcha,
    onThirdLogin,
    sendCodeApi,
  };
}
