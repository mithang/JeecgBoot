<template>
  <!-- Hộp thoại nhập mật khẩu liên kết tài khoản khi đăng nhập bên thứ ba -->
  <a-modal title="Vui lòng nhập mật khẩu" v-model:open="thirdPasswordShow" @ok="thirdLoginCheckPassword" @cancel="thirdLoginNoPassword">
    <a-input-password placeholder="Vui lòng nhập mật khẩu" v-model:value="thirdLoginPassword" style="margin: 15px; width: 80%" />
  </a-modal>

  <!-- Hộp thoại nhắc có liên kết tài khoản hay không khi đăng nhập bên thứ ba -->
  <a-modal :footer="null" :closable="false" v-model:open="thirdConfirmShow" :class="'ant-modal-confirm'">
    <div class="ant-modal-confirm-body-wrapper">
      <div class="ant-modal-confirm-body">
        <QuestionCircleFilled style="color: #faad14" />
        <span class="ant-modal-confirm-title">Nhắc nhở</span>
        <div class="ant-modal-confirm-content"> Đã tồn tại tài khoản cùng tên,Vui lòng xác nhận có liên kết tài khoản này không? </div>
      </div>
      <div class="ant-modal-confirm-btns">
        <a-button @click="thirdLoginUserCreate" :loading="thirdCreateUserLoding">Tạo tài khoản mới</a-button>
        <a-button @click="thirdLoginUserBind" type="primary">Xác nhận liên kết</a-button>
      </div>
    </div>
  </a-modal>

  <!-- Đăng nhập bên thứ ba liên kết số điện thoại -->
  <a-modal title="Liên kết số điện thoại" v-model:open="bindingPhoneModal" :maskClosable="false">
    <Form class="p-4 enter-x" style="margin: 15px 10px">
      <FormItem class="enter-x">
        <a-input size="large" placeholder="Vui lòng nhập số điện thoại" v-model:value="thirdPhone" class="fix-auto-fill">
          <template #prefix>
            <Icon icon="ant-design:mobile-outlined" :style="{ color: 'rgba(0,0,0,.25)' }"></Icon>
          </template>
        </a-input>
      </FormItem>
      <FormItem name="sms" class="enter-x">
        <CountdownInput size="large" class="fix-auto-fill" v-model:value="thirdCaptcha" placeholder="Vui lòng nhập mã xác thực" :sendCodeApi="sendCodeApi">
          <template #prefix>
            <Icon icon="ant-design:mail-outlined" :style="{ color: 'rgba(0,0,0,.25)' }"></Icon>
          </template>
        </CountdownInput>
      </FormItem>
    </Form>
    <template #footer>
      <a-button type="primary" @click="thirdHandleOk">Xác nhận</a-button>
    </template>
  </a-modal>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { Form, Input } from 'ant-design-vue';
  import { CountdownInput } from '/@/components/CountDown';
  import { useThirdLogin } from '/@/hooks/system/useThirdLogin';
  import { QuestionCircleFilled } from '@ant-design/icons-vue';

  const FormItem = Form.Item;
  const InputPassword = Input.Password;

  export default defineComponent({
    name: 'ThirdModal',
    components: { FormItem, Form, InputPassword, CountdownInput, QuestionCircleFilled },
    setup() {
      return {
        ...useThirdLogin(),
      };
    },
  });
</script>
