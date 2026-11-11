<template>
  <div class="step2">
    <a-alert message="Sau khi xác nhận chuyển khoản, tiền sẽ được chuyển trực tiếp vào tài khoản của đối phương và không thể hoàn lại." show-icon />
    <a-descriptions :column="1" class="mt-5">
      <a-descriptions-item label="Tài khoản thanh toán"> ant-design@alipay.com </a-descriptions-item>
      <a-descriptions-item label="Tài khoản nhận tiền"> test@example.com </a-descriptions-item>
      <a-descriptions-item label="Tên người nhận tiền"> Jeecg </a-descriptions-item>
      <a-descriptions-item label="Số tiền chuyển khoản"> 500đồng </a-descriptions-item>
    </a-descriptions>
    <a-divider />
    <BasicForm @register="register" />
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicForm, useForm } from '/@/components/Form';
  import { step2Schemas } from './data';
  import { Alert, Divider, Descriptions } from 'ant-design-vue';

  export default defineComponent({
    components: {
      BasicForm,
      [Alert.name]: Alert,
      [Divider.name]: Divider,
      [Descriptions.name]: Descriptions,
      [Descriptions.Item.name]: Descriptions.Item,
    },
    emits: ['next', 'prev'],
    setup(_, { emit }) {
      const [register, { validate, setProps }] = useForm({
        labelWidth: 120,
        schemas: step2Schemas,
        actionColOptions: {
          span: 14,
        },
        resetButtonOptions: {
          text: 'Bước trước',
        },
        submitButtonOptions: {
          text: 'Gửi',
        },
        resetFunc: customResetFunc,
        submitFunc: customSubmitFunc,
      });

      async function customResetFunc() {
        emit('prev');
      }

      async function customSubmitFunc() {
        try {
          const values = await validate();
          setProps({
            submitButtonOptions: {
              loading: true,
            },
          });
          setTimeout(() => {
            setProps({
              submitButtonOptions: {
                loading: false,
              },
            });
            emit('next', values);
          }, 1500);
        } catch (error) {}
      }

      return { register };
    },
  });
</script>
<style lang="less" scoped>
  .step2 {
    width: 550px;
    margin: 0 auto;
  }
</style>
