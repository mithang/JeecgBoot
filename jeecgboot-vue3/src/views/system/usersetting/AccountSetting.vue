<template>
  <div :class="[`${prefixCls}`]">
    <div class="my-account">Tài khoản ngân hàng</div>
    <div class="account-row-item clearfix">
      <div class="account-label gray-75">Điện thoại</div>
      <span class="gray" v-if="userDetail.phoneText">{{ userDetail.phoneText}}</span>
      <span class="pointer blue-e5 phone-margin" @click="updatePhone" v-if="userDetail.phone">Sửa</span>
      <span class="pointer blue-e5 phone-margin" @click="bindPhone" v-else>Liên kết</span>
      <!--      <span class="pointer blue-e5" @click="unbindPhone" v-if="userDetail.phone">Gỡ liên kết?</span>-->
      <!--      <span class="pointer blue-e5" @click="unbindPhone" v-else>Liên kết?</span>-->
    </div>
    <div class="account-row-item clearfix">
      <div class="account-label gray-75">Email</div>
      <span class="gray">{{ userDetail.email ? userDetail.email : 'Chưa điền' }}</span>
      <span class="pointer blue-e5 phone-margin" @click="updateEmail">Sửa</span>
      <!--      <span class="pointer blue-e5" @click="unbindEmail" v-if="userDetail.email">Gỡ liên kết?</span>-->
      <!--      <span class="pointer blue-e5" @click="unbindEmail" v-else>Liên kết?</span>-->
      <!--      <span class="pointer blue-e5" style="margin-left:5px" @click="checkEmail" v-if="userDetail.email">Xác thực?</span>-->
    </div>
    <div class="account-row-item">
      <div class="account-label gray-75">Mật khẩu</div>
      <Icon icon="ant-design:lock-outlined" style="color: #9e9e9e" />
      <span class="pointer blue-e5" style="margin-left: 10px" @click="updatePassWord">Sửa</span>
    </div>

    <!--    <div class="account-row-item clearfix">-->
    <!--      <div class="account-label gray-75">Hủy tài khoản (account)?</div>-->
    <!--      <span style="color: red" class="pointer" @click="cancellation">Hủy tài khoản?</span>-->
    <!--    </div>-->
  </div>

  <UserReplacePhoneModal @register="registerModal" @success="initUserDetail" />
  <UserReplaceEmailModal @register="registerEmailModal" @success="initUserDetail" />
  <UserPasswordModal @register="registerPassModal" @success="initUserDetail" />
  <UserPasswordNotBindPhone @register="registerPassNotBindPhoneModal" @success="initUserDetail" />
  <UserCancellationModal @register="registerCancelModal" />
</template>
<script lang="ts" setup>
  import { onMounted, ref, reactive } from 'vue';
  import { CollapseContainer } from '/@/components/Container';
  import { getUserData } from './UserSetting.api';
  import { useUserStore } from '/@/store/modules/user';
  import UserReplacePhoneModal from './commponents/UserPhoneModal.vue';
  import UserReplaceEmailModal from './commponents/UserEmailModal.vue';
  import UserPasswordModal from './commponents/UserPasswordModal.vue';
  import UserPasswordNotBindPhone from './commponents/UserPasswordNotBindPhone.vue';
  import UserCancellationModal from './commponents/UserCancellationModal.vue';
  import { useModal } from '/@/components/Modal';
  import { WechatFilled } from '@ant-design/icons-vue';
  import { useDesign } from '/@/hooks/web/useDesign';

  const { prefixCls } = useDesign('j-user-account-setting-container');

  const userDetail = ref<any>([]);
  const userStore = useUserStore();
  const [registerModal, { openModal }] = useModal();
  const [registerEmailModal, { openModal: openEmailModal }] = useModal();
  const [registerPassModal, { openModal: openPassModal }] = useModal();
  const [registerPassNotBindPhoneModal, { openModal: openPassNotBindPhoneModal }] = useModal();
  const [registerCancelModal, { openModal: openCancelModal }] = useModal();

  const wechatData = reactive<any>({
    bindWechat: false,
    name: 'Biệt danh',
  });

  /**
   * Khởi tạo dữ liệu người dùng
   */
  function initUserDetail() {
    //Lấy dữ liệu người dùng
    getUserData().then((res) => {
      if (res.success) {
        userDetail.value = res.result;
        if(res.result.phone){
          userDetail.value.phoneText = res.result.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
        }
      }
    });
  }

  /**
   * Đổi số điện thoại
   */
  function updatePhone() {
    openModal(true, {
      record: { phone: userDetail.value.phone, username: userDetail.value.username, id: userDetail.value.id, phoneText: userDetail.value.phoneText },
    });
  }
  
  /**
   * Liên kết số điện thoại
   */ 
  function bindPhone() {
    openModal(true, {
      record: { username: userDetail.value.username, id: userDetail.value.id },
    });
  }

  /**
   * Đổi email
   */
  function updateEmail() {
    openEmailModal(true, {
      record: { email: userDetail.value.email, id: userDetail.value.id },
    });
  }

  /**
   * Đổi mật khẩu
   */
  function updatePassWord() {
    //Có số điện thoại, dùng số điện thoại để đổi mật khẩu
    if(userDetail.value.phone){
      openPassModal(true, {
        record: { username: userDetail.value.username },
      });
    } else {
      //Không có số điện thoại thì đi tới hộp thoại đổi mật khẩu trực tiếp
      openPassNotBindPhoneModal(true, {
        record: { username: userDetail.value.username },
      });
    }
  }

  /**
   * Gỡ liên kết số điện thoại
   */
  function unbindPhone() {
    console.log('Gỡ liên kết số điện thoại');
  }

  /**
   * Gỡ liên kết email
   */
  function unbindEmail() {
    console.log('Gỡ liên kết email');
  }

  /**
   * Xác thực email
   */
  function checkEmail() {
    console.log('Xác thực email');
  }

  /**
   * Sự kiện liên kết/gỡ liên kết WeChat
   */
  function wechatBind() {
    console.log('Sự kiện liên kết/gỡ liên kết WeChat');
  }

  /**
   * Sự kiện hủy tài khoản
   */
  function cancellation() {}

  onMounted(() => {
    initUserDetail();
  });
</script>
<style lang="less">
    // Giải thích logic mã: [issues/563]Một phần chủ đề tối không hoạt động
  @prefix-cls: ~'@{namespace}-j-user-account-setting-container';

  .@{prefix-cls}{
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
      content: "";
      display: table;
    }
    .my-account{
      font-size: 17px;
      font-weight: 700!important;
      /*begin Tương thích chế độ tối*/
      color: @text-color;
      /*end Tương thích chế độ tối*/
      margin-bottom: 20px;
    }
  }
</style>
