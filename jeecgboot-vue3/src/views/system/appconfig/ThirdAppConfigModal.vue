<template>
  <BasicModal @register="registerModal" :width="800" :bodyStyle="{minHeight: '450px'}" :title="title" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useForm, BasicForm } from '/@/components/Form';
  import { thirdAppFormSchema } from './ThirdApp.data';
  import { getThirdConfigByTenantId, saveOrUpdateThirdConfig } from './ThirdApp.api';
  export default defineComponent({
    name: 'ThirdAppConfigModal',
    components: { BasicModal, BasicForm },
    setup(props, { emit }) {
      const title = ref<string>('Cấu hình DingTalk');
      //Cấu hình biểu mẫu
      const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
        schemas: thirdAppFormSchema,
        showActionButtonGroup: false,
        labelCol: { span: 24 },
        wrapperCol: { span: 24 },
      });
      //Gán giá trị cho biểu mẫu
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: true });
        if (data.thirdType == 'dingtalk') {
          title.value = 'Cấu hình DingTalk';
        } else if (data.thirdType == 'feishu') {
          title.value = 'Cấu hình Feishu';
        } else {
          title.value = 'Cấu hình WeCom';
        }
        //Đặt lại biểu mẫu
        await resetFields();
        let values = await getThirdConfigByTenantId({ tenantId: data.tenantId, thirdType: data.thirdType });
        setModalProps({ confirmLoading: false });
        //Gán giá trị cho biểu mẫu
        if (values) {
          await setFieldsValue(values);
        } else {
          await setFieldsValue(data);
        }
      });

      /**
       * Sự kiện nhấp vào cấu hình bên thứ ba
       */
      async function handleSubmit() {
        let values = await validate();
        let isUpdate = false;
        if (values.id) {
          isUpdate = true;
        }
        await saveOrUpdateThirdConfig(values, isUpdate);
        emit('success');
        closeModal();
      }

      return {
        title,
        registerForm,
        registerModal,
        handleSubmit,
      };
    },
  });
</script>

<style scoped></style>
