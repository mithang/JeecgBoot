import { ref, unref, defineEmits } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { useGlobSetting } from '/@/hooks/setting';
import { useMessage } from '/@/hooks/web/useMessage';
import { useUserStore } from '/@/store/modules/user';
import { setThirdCaptcha, getCaptcha } from '/@/api/sys/user';
import { useI18n } from '/@/hooks/web/useI18n';

export function useThirdLogin(emit) {
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
  //Đăng ký hoặc liên kết tài khoản
  const bindingAccount = ref(false);
  //Người dùng bên thứ baUUID
  const thirdUserUuid = ref('');
  //Cửa sổ nhắc nhở
  const thirdConfirmShow = ref(false);
  //Liên kết số điện thoại
  const thirdPhone = ref('');
  //Mã xác thực
  const thirdCaptcha = ref('');
  //Đăng nhập bên thứ ba
  function onThirdLogin(source) {
    let url = `${glob.uploadUrl}/sys/thirdLogin/render/${source}`;
    window.open(
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
        //Nếu là kiểu chuỗi Cho thấy làtokenThông tin
        if (token === '登录失败') {
          createMessage.warning(token);
        } else if (token.includes('绑定手机号')) {
          bindingAccount.value = true;
          let strings = token.split(',');
          thirdUserUuid.value = strings[1];
          emit('type', { loginType: 'thirdLogin' });
        } else {
          doThirdLogin(token);
        }
      } else if (typeof token === 'object') {
        //Loại đối tượng Cho thấy cần nhắc có liên kết tài khoản hiện có hay không
        if (token['isObj'] === true) {
          thirdConfirmShow.value = true;
          thirdLoginInfo.value = { ...token };
        }
      } else {
        createMessage.warning('Truyền tải thông tin không nhận diện được');
      }
      //update-begin---author:wangshuai---date:2024-02-20---for:【QQYUN-8156】Đăng nhập thất bại liên tục khiến nhắc nhở thất bại cộng dồn---
      window.removeEventListener('message', unref(receiveMessage),false);
      //update-end---author:wangshuai---date:2024-02-20---for:【QQYUN-8156】Đăng nhập thất bại liên tục khiến nhắc nhở thất bại cộng dồn---
    };
    window.addEventListener('message', receiveMessage, false);
  }
  // Căn cứ theotokenThực thi đăng nhập
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
      description: ((err.response || {}).data || {}).message || err.message || 'Yêu cầu xuất hiện lỗi, vui lòng thử lại sau',
      duration: 4,
    });
  }

  //Liên kết số điện thoại, nhấp nút xác nhận
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
        bindingAccount.value = false;
        doThirdLogin(res.result);
      } else {
        createMessage.warning(res.message);
      }
    }).catch((e)=>{
      createMessage.warning(e.message);
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

  /**
   * Đăng nhập và liên kết
   */
  function loginAccountClick() {
    emit('type', { loginType: 'login' });
  }

  /**
   * Tạo tài khoản mới
   */
  function registerAccountClick() {
    emit('type', { loginType: 'register' });
  }

  /**
   * Ẩn trang liên kết
   */
  function hideBindThirdAccount() {
    bindingAccount.value = false;
  }

  /**
   * Đăng ký người dùng đồng thời liên kết bên thứ ba
   */
  async function createAccountBindThird(values) {
    let params = { ...values };
    params.thirdUserUuid = unref(thirdUserUuid);
    await defHttp
      .put({ url: '/sys/thirdLogin/registerBindThirdAccount', params }, { isTransformResponse: false })
      .then((res) => {
        if (res.success) {
          bindingAccount.value = false;
          doThirdLogin(res.result);
        } else {
          createMessage.warning(res.message);
        }
      })
      .catch((e) => {
        createMessage.warning(e.message);
      })
  }

  /**
   * Liên kết số điện thoại
   * @param values
   */
  function bindThirdAccount(values) {
    thirdPhone.value = values.mobile;
    thirdCaptcha.value = values.sms;
    thirdHandleOk();
  }

  //Trả về dữ liệu và phương thức
  return {
    thirdConfirmShow,
    bindingAccount,
    thirdHandleOk,
    thirdPhone,
    thirdCaptcha,
    onThirdLogin,
    loginAccountClick,
    registerAccountClick,
    hideBindThirdAccount,
    bindThirdAccount,
    createAccountBindThird,
  };
}
