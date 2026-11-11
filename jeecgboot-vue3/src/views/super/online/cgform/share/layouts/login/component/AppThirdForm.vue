<template>
  <!-- Liên kết tài khoản hoặc đăng ký -->
  <div class="third-account" v-show="bindingAccount">
    <div class="content">
      <div class="bind-title">
        <span>Chưa liên kết tài khoản Qiaoqiao Cloud</span>
      </div>
      <div class="bind-title-desc">
        <span>Vui lòng chọn liên kết tài khoản đã có, hoặc tạo tài khoản mới</span>
      </div>
      <div class="enter-x bind-btn">
        <a-button type="primary" @click="loginAccountClick">Đăng nhập và liên kết</a-button>
      </div>
      <div class="enter-x bind-btn" style="margin-top: 20px">
        <a-button type="primary" @click="registerAccountClick">Đăng ký tài khoản mới</a-button>
      </div>
    </div>
  </div>
</template>
<script lang="ts" name="app-third-form">
  import { defineComponent } from 'vue';
  import { Form, Input } from 'ant-design-vue';
  import { CountdownInput } from '/@/components/CountDown';
  import { useThirdLogin } from '../AppThirdLogin';
  import { QuestionCircleFilled } from '@ant-design/icons-vue';

  const FormItem = Form.Item;
  const InputPassword = Input.Password;

  export default defineComponent({
    name: 'AppThirdForm',
    components: { FormItem, Form, InputPassword, CountdownInput, QuestionCircleFilled },
    setup(_p, { emit }) {
      return {
        ...useThirdLogin(emit),
      };
    },
  });
</script>
<style lang="less" scoped>
  .third-account {
    width: 100%;
    .content {
      width: 350px;
      text-align: center;
      margin: 60px auto 0;
      padding: 30px;
      background: #ffffff;
      box-shadow: 0 2px 6px 0 rgba(0, 0, 0, 0.15);
      border-radius: 3px;
    }
    .bind-title {
      font-size: 18px;
      color: #333333;
      margin-bottom: 15px;
    }
    .bind-title-desc {
      font-size: 13px;
      color: #aaa;
      margin-bottom: 30px;
    }
    .bind-btn {
      button {
        color: white;
        height: 44px;
        text-align: center;
        border-radius: 4px;
        -moz-border-radius: 4px;
        -webkit-border-radius: 4px;
        font-size: 14px;
        width: 100%;
      }
    }
  }
</style>
