<!--Hộp thoại mời người dùng tham gia đơn vị thuê-->
<template>
  <BasicModal @register="registerModal" :width="500" :title="title" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts">
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { defineComponent, ref } from 'vue';
export default defineComponent({
  name: 'TenantInviteUserModal',
  components: {
    BasicModal,
    BasicForm,
  },
  setup(props, { emit }) {
    const title = ref<string>('Mời thành viên');
    const [registerForm, { resetFields, validate }] = useForm({
      schemas: [
        {
          label: 'Cách mời',
          field: 'invitedMode',
          component: 'RadioButtonGroup',
          defaultValue: 1,
          componentProps: {
            options: [
              { label: 'Số điện thoại', value: 1 },
              { label: 'Tài khoản người dùng', value: 2 },
            ],
          },
        },
        {
          label: 'Số điện thoại',
          field: 'phone',
          component: 'Input',
          ifShow: ({ values }) => values.invitedMode === 1,
          dynamicRules: ({ values }) => {
            return values.invitedMode === 1
              ? [
                  { required: true, message: 'Vui lòng điền số điện thoại' },
                  { pattern: /^1[3456789]\d{9}$/, message: 'Định dạng số điện thoại không đúng' },
                ]
              : [{ pattern: /^1[3456789]\d{9}$/, message: 'Định dạng số điện thoại không đúng' }];
          },
        },
        {
          field: 'user',
          component: 'Input',
          label: 'Tài khoản người dùng',
          ifShow: ({ values }) => values.invitedMode === 2,
          dynamicRules: ({ values }) => {
            return values.invitedMode === 2 ? [{ required: true, message: 'Vui lòng nhập tài khoản người dùng' }] : [];
          },
        },
      ],
      showActionButtonGroup: false,
    });
    //Gán giá trị cho biểu mẫu
    const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
      //Đặt lại biểu mẫu
      await resetFields();
      setModalProps({ minHeight: 100 });
    });

    /**
     * Gửi, trả về trang list của đơn vị thuê
     */
    async function handleSubmit() {
      let values = await validate();
      emit('inviteOk', values.phone, values.user);
      closeModal();
    }

    return {
      title,
      registerModal,
      registerForm,
      handleSubmit,
    };
  },
});
</script>

<style scoped></style>
