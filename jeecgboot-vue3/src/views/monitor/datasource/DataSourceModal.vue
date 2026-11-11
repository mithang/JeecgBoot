<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="40%">
    <BasicForm @register="registerForm">
      <template #pwd="{ model, field }">
        <a-row :gutter="8">
          <a-col :sm="15" :md="16" :lg="17" :xl="19">
            <a-input-password v-model:value="model[field]" placeholder="Vui lòng nhập mật khẩu" autocomplete="new-password" />
          </a-col>
          <a-col :sm="9" :md="7" :lg="7" :xl="5">
            <a-button type="primary" style="width: 100%" @click="handleTest">Kiểm tra</a-button>
          </a-col>
        </a-row>
        <div v-if="isUpdate" style="color: #ff4d4f; line-height: 1.5; margin-top: 2px">Để trống mật khẩu nghĩa là không thay đổi, kiểm tra và lưu đều dùng mật khẩu gốc</div>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './datasource.data';
  import { saveOrUpdateDataSource, getDataSourceById, testConnection } from './datasource.api';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  // Khai báo Emits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  // Cấu hình biểu mẫu
  const [registerForm, { getFieldsValue, resetFields, validateFields, setFieldsValue, validate }] = useForm({
    // labelWidth: 150,
    schemas: formSchema,
    showActionButtonGroup: false,
  });
  // Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      // Lấy chi tiết
      data.record = await getDataSourceById({ id: data.record.id });
      // [Tăng cường bảo mật] Khi chỉnh sửa không điền lại mật khẩu, để trống nghĩa là không sửa; kiểm tra và lưu đều do backend theo id/code dùng mật khẩu gốc
      await setFieldsValue({
        ...data.record,
        dbPassword: '',
      });
    }
  });
  // Cài đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới nguồn dữ liệu' : 'Chỉnh sửa nguồn dữ liệu'));

  async function handleTest() {
    let keys = ['dbType', 'dbDriver', 'dbUrl', 'dbUsername'];
    await validateFields(keys).then(() => {
      // [Tăng cường bảo mật] Lấy tất cả các trường (bao gồm id, code); khi mật khẩu để trống thì backend theo id/code dùng mật khẩu gốc để kiểm tra
      let values = getFieldsValue();
      let loading = createMessage.loading('Đang kết nối....', 0);
      testConnection(values)
        .then((data) => {
          if (data.success) {
            createMessage.success('Kết nối thành công');
          }
        })
        .catch((error) => {})
        .finally(() => loading());
    });
  }

  // Sự kiện gửi biểu mẫu
  async function handleSubmit(v) {
    try {
      let values = await validate();
      // [Tăng cường bảo mật] Mật khẩu để trống nghĩa là không sửa, backend dùng mật khẩu gốc để lưu
      setModalProps({ confirmLoading: true });
      // Gửi biểu mẫu
      await saveOrUpdateDataSource(values, isUpdate.value);
      // Đóng popup
      closeModal();
      // Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
