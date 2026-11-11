<template>
  <BasicModal :title="title" :width="800" v-bind="$attrs" @ok="handleOk" @register="registerModal">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, inject, ref, unref } from 'vue';

  import { BasicForm, useForm } from '/@/components/Form/index';
  // noinspection ES6UnusedImports
  import { BasicModal, useModalInner } from '/@/components/Modal';

  import { saveOrUpdateDepartRole } from '../depart.user.api';
  import { departRoleModalFormSchema } from '../depart.user.data';

  const emit = defineEmits(['success', 'register']);
  const props = defineProps({
    // Phòng ban hiện tạiID
    departId: { require: true, type: String },
  });
  const prefixCls = inject('prefixCls');
  // Hiện có đang ở chế độ cập nhật không
  const isUpdate = ref<boolean>(true);
  // Dữ liệu của hộp thoại hiện tại
  const model = ref<object>({});
  const title = computed(() => (isUpdate.value ? 'Chỉnh sửa' : 'Thêm mới'));

  //Biểu mẫu đăng ký
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: departRoleModalFormSchema,
    showActionButtonGroup: false,
  });

  // Hộp thoại đăng ký
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    isUpdate.value = unref(data?.isUpdate);
    // Dù thêm mới hay chỉnh sửa đều có thể đặt giá trị cho biểu mẫu
    let record = unref(data?.record);
    if (typeof record === 'object') {
      model.value = record;
      await setFieldsValue({ ...record });
    }
  });

  //Sự kiện gửi
  async function handleOk() {
    try {
      setModalProps({ confirmLoading: true });
      let values = await validate();
      values.departId = unref(props.departId);
      //Gửi biểu mẫu
      await saveOrUpdateDepartRole(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách
      emit('success', { isUpdate: unref(isUpdate), values });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
