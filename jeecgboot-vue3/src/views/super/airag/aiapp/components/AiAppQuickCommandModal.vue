<template>
  <div>
    <BasicModal destroyOnClose @register="registerModal" :canFullscreen="false" width="800px" :title="title" @ok="handleOk" @cancel="handleCancel">
      <BasicForm @register="registerForm"></BasicForm>
    </BasicModal>
  </div>
</template>

<script lang="ts">
import { ref, unref } from 'vue';
import BasicModal from '@/components/Modal/src/BasicModal.vue';
import { useModalInner } from '@/components/Modal';

import BasicForm from '@/components/Form/src/BasicForm.vue';
import { useForm } from '@/components/Form';
import { quickCommandFormSchema} from '../AiApp.data';

export default {
  name: 'AiAppQuickCommandModal',
  components: {
    BasicForm,
    BasicModal,
  },
  emits: ['ok', 'update-ok', 'register'],
  setup(props, { emit }) {
    const title = ref<string>('Thêm lệnh');

    //Lưu hoặc sửa đổi
    const isUpdate = ref<boolean>(false);

    //Cấu hình biểu mẫu
    const [registerForm, { validate, resetFields, setFieldsValue }] = useForm({
      schemas: quickCommandFormSchema,
      showActionButtonGroup: false,
      layout: 'vertical',
      wrapperCol: { span: 24 },
    });

    //Đăng ký modal
    const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data) => {
      await resetFields();
      isUpdate.value = !!data?.isUpdate;
      if (unref(isUpdate)) {
        //Gán giá trị biểu mẫu
        await setFieldsValue({
          ...data.record,
        });
      }
      setModalProps({ minHeight: 200, bodyStyle: { padding: '10px' } });
    });

    /**
     * Lưu
     */
    async function handleOk() {
      try {
        let values = await validate();
        setModalProps({ confirmLoading: true });
        if(isUpdate.value){
          emit('update-ok',values);
        }else{
          emit('ok', values);
        }
        handleCancel();
      } finally {
        setModalProps({ confirmLoading: false });
      }
    }

    /**
     * Hủy
     */
    function handleCancel() {
      closeModal();
    }

    return {
      registerModal,
      registerForm,
      title,
      handleOk,
      handleCancel
    };
  },
};
</script>

<style scoped lang="less">
.pointer {
  cursor: pointer;
}
.type-title {
  color: #1d2025;
  margin-bottom: 4px;
}
.type-desc {
  color: #8f959e;
  font-weight: 400;
}
</style>
