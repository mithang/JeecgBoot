<template>
  <div :class="[`${prefixCls}`]">
    <div class="my-account">Bên thứ baAPP</div>
<!--    <div class="account-row-item">-->
<!--      <div class="account-label gray-75">Liên kết WeCom</div>-->
<!--      <span>-->
<!--        <icon-font :style="!bindEnterpriseData.sysUserId ? { color: '#9e9e9e' } : { color: '#0082EF' }" class="item-icon" type="icon-qiyeweixin3" />-->
<!--        <span class="gray-75" style="margin-left: 12px">WeCom</span>-->
<!--        <span class="gray-75" style="margin-left: 8px" v-if="bindEnterpriseData.realname">{{ 'Đã liên kết:' + bindEnterpriseData.realname }}</span>-->
<!--        <span class="blue-e5 pointer" style="margin-left: 24px" @click="wechatEnterpriseBind">{{-->
<!--          !bindEnterpriseData.sysUserId ? 'Liên kết' : 'Gỡ liên kết'-->
<!--        }}</span>-->
<!--      </span>-->
<!--    </div>-->
    <div class="account-row-item">
      <div class="account-label gray-75">Liên kết DingTalk</div>
      <span>
        <DingtalkCircleFilled :style="!bindDingData.sysUserId ? { color: '#9e9e9e' } : { color: '#007FFF' }" class="item-icon" />
        <span class="gray-75" style="margin-left: 12px">DingTalk</span>
        <span class="gray-75" style="margin-left: 8px" v-if="bindDingData.realname">{{ 'Đã liên kết:' + bindDingData.realname }}</span>
        <span class="blue-e5 pointer" style="margin-left: 24px" @click="dingDingBind">{{ !bindDingData.sysUserId ? 'Liên kết' : 'Gỡ liên kết' }}</span>
      </span>
    </div>
    <div class="account-row-item">
      <div class="account-label gray-75">Liên kết tài khoản</div>
      <span>
        <WechatFilled :style="!bindWechatData.sysUserId ? { color: '#9e9e9e' } : { color: '#1ec563' }" class="item-icon" />
        <span class="gray-75" style="margin-left: 12px">WeChat</span>
        <span class="gray-75" style="margin-left: 8px" v-if="bindWechatData.realname">{{ 'Đã liên kết:' + bindWechatData.realname }}</span>
        <span class="blue-e5 pointer" style="margin-left: 24px" @click="wechatBind">{{ !bindWechatData.sysUserId ? 'Liên kết' : 'Gỡ liên kết' }}</span>
      </span>
    </div>
  </div>
</template>
<script lang="ts" setup name="we-chat-ding-setting">
  import { onMounted, ref, reactive, unref } from 'vue';
  import { CollapseContainer } from '/@/components/Container';
  import { bindThirdAppAccount, deleteThirdAccount, getThirdAccountByUserId } from './UserSetting.api';
  import { useUserStore } from '/@/store/modules/user';
  import { useModal } from '/@/components/Modal';
  import { DingtalkCircleFilled, WechatFilled } from '@ant-design/icons-vue';
  import { IconFont } from '/@/utils/iconfont2';
  import { useGlobSetting } from '/@/hooks/setting';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Modal } from 'ant-design-vue';
  import { useDesign } from '/@/hooks/web/useDesign';

  const { prefixCls } = useDesign('j-user-tenant-setting-container');

  const userStore = useUserStore();

  //Dữ liệu liên kết WeChat
  const bindWechatData = ref<any>({});
  //Dữ liệu liên kết DingTalk
  const bindDingData = ref<any>({});
  //Dữ liệu liên kết WeCom
  const bindEnterpriseData = ref<any>({});

  const glob = useGlobSetting();
  //Loại bên thứ ba
  const thirdType = ref('');
  //Người dùng bên thứ baUUID
  const thirdUserUuid = ref('');
  //Chi tiết bên thứ ba
  const thirdDetail = ref<any>({});
  const { createMessage } = useMessage();
  //windowsĐối tượng, dùng cho sự kiện đóng cửa sổ
  const windowsIndex = ref<any>('');
  //Sự kiện lắng nghe cửa sổ
  const receiveMessage = ref<any>('');
  
  /**
   * Khởi tạo dữ liệu DingTalk và WeCom
   */
  async function initUserDetail() {
    let values = await getThirdAccountByUserId({ thirdType: 'wechat_open,dingtalk,wechat_enterprise' });
    bindWechatData.value = "";
    bindDingData.value = "";
    bindEnterpriseData.value = "";
    if (values && values.result) {
      let result = values.result;
      for (let i = 0; i < result.length; i++) {
        setThirdDetail(result[i]);
      }
    }
  }

  /**
   * Sự kiện liên kết/gỡ liên kết WeCom
   */
  function wechatEnterpriseBind() {
    console.log('Sự kiện liên kết/gỡ liên kết WeCom');
    let data = unref(bindEnterpriseData);
    if (!data.sysUserId) {
      onThirdLogin('wechat_enterprise');
    }else{
      deleteAccount({ sysUserId: data.sysUserId, id: data.id }, 'WeCom');
    }
  }

  /**
   * Sự kiện liên kết/gỡ liên kết DingTalk
   */
  function dingDingBind() {
    let data = unref(bindDingData);
    if (!data.sysUserId) {
      onThirdLogin('dingtalk');
    } else {
      deleteAccount({ sysUserId: data.sysUserId, id: data.id }, 'DingTalk');
    }
    console.log('Sự kiện liên kết/gỡ liên kết DingTalk');
  }

  /**
   * Liên kết WeChat
   */
  function wechatBind() {
    let data = unref(bindWechatData);
    if (!data.sysUserId) {
      onThirdLogin('wechat_open');
    }else{
      deleteAccount({ sysUserId: data.sysUserId, id: data.id }, 'WeChat');
    }
  }

  /**
   * Đăng nhập bên thứ ba
   * @param source
   */
  function onThirdLogin(source) {
    let url = `${glob.uploadUrl}/sys/thirdLogin/render/${source}`;
    //Cửa sổ không trống thì đóng
    console.log("unref(windowsIndex) ::",unref(windowsIndex))
    if(unref(windowsIndex)){
      //Đảm bảo chỉ có một cửa sổ
      windowsIndex.value.close();
      //Đảm bảo chỉ có một trình lắng nghe
      window.removeEventListener('message', unref(receiveMessage),false);
    }
    
    windowsIndex.value = window.open(
      url,
      `login ${source}`,
      'height=500, width=500, top=0, left=0, toolbar=no, menubar=no, scrollbars=no, resizable=no,location=n o, status=no'
    );
    thirdType.value = source;
    receiveMessage.value = async function (event) {
      let token = event.data;
      if (typeof token === 'string') {
        //Nếu là loại chuỗi thì nghĩa là thông tin token
        if (token === 'Đăng nhập thất bại') {
          cmsFailed();
        } else if (token.includes('Liên kết số điện thoại')) {
          let strings = token.split(',');
          thirdUserUuid.value = strings[1];
          await bindThirdAccount();
        }else{
          if(token){
            createMessage.warning('Tài khoản Qiaoqiao Cloud này đã bị liên kết bởi tài khoản bên thứ ba khác,Vui lòng gỡ liên kết hoặc liên kết tài khoản Qiaoqiao Cloud khác');
          }
        }
      } else {
        cmsFailed();
      }
      window.removeEventListener('message', unref(receiveMessage),false);
      windowsIndex.value = "";
    };
    window.addEventListener('message', unref(receiveMessage), false);
  }

  /**
   * Liên kết người dùng hiện tại
   */
  async function bindThirdAccount() {
    if (!unref(thirdUserUuid)) {
      cmsFailed();
      return;
    }
    let params = { thirdUserUuid: unref(thirdUserUuid), thirdType: unref(thirdType) };
    await bindThirdAppAccount(params)
      .then((res) => {
        if (res.success) {
          if (res.result) {
            setThirdDetail(res.result);
          }
        } else {
          createMessage.warning(res.message);
        }
      })
      .catch((res) => {
        createMessage.warning(res.message);
      });
  }

  /**
   * Thông tin nhắc thất bại
   */
  function cmsFailed() {
    createMessage.warning('Liên kết tài khoản bên thứ ba bất thường');
    return;
  }

  /**
   * Đặt dữ liệu bên thứ ba
   * @param value
   */
  function setThirdDetail(value) {
    let type = value.thirdType;
    if (type == 'wechat_open') {
      bindWechatData.value = value;
    } else if (type == 'dingtalk') {
      bindDingData.value = value;
    } else if (type == 'wechat_enterprise') {
      bindEnterpriseData.value = value;
    }
  }

  /**
   * Xóa bảng thông tin bên thứ ba
   * @param params
   */
  async function deleteAccount(params, text) {
    Modal.confirm({
      title: 'Gỡ liên kết' + text,
      content: 'Bạn có chắc muốn gỡ liên kết không',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: async () => {
        await deleteThirdAccount(params).then((res) =>{
          if(res.success){
            initUserDetail();
            createMessage.success(res.message)
          }else{
            createMessage.warning(res.message)
          }
        });
      },
    });
  }

  onMounted(() => {
    initUserDetail();
  });
</script>
<style lang="less">
// Giải thích logic mã: [issues/563]Một phần chủ đề tối không hoạt động
@prefix-cls: ~'@{namespace}-j-user-tenant-setting-container';
.@{prefix-cls} {
   padding: 30px 40px 0 20px;
  .account-row-item {
    align-items: center;
    /*begin Tương thích chế độ tối*/
    border-bottom: 1px solid @border-color-base;
    /*end Tương thích chế độ tối*/
    box-sizing: border-box;
    display: flex;
    height: 71px;
    position: relative;
  }

  .account-label {
    text-align: left;
    width: 160px;
  }

  .gray-75 {
    /*begin Tương thích chế độ tối*/
    color: @text-color !important;
    /*end Tương thích chế độ tối*/
  }

  .pointer {
    cursor: pointer;
  }

  .blue-e5 {
    color: #1e88e5;
  }

  .phone-margin {
    margin-left: 24px;
    margin-right: 24px;
  }

  .clearfix:after {
    clear: both;
  }

  .clearfix:before {
    content: '';
    display: table;
  }

  .my-account {
    font-size: 17px;
    font-weight: 700 !important;
    /*begin Tương thích chế độ tối*/
    color: @text-color !important;
    /*end Tương thích chế độ tối*/
    margin-bottom: 20px;
  }
  .item-icon {
    font-size: 16px !important;
  }
}
</style>
