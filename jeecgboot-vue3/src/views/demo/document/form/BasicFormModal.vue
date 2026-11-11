<!-- Biểu mẫu lớp popup -->
<template>
  <div style="margin: 20px auto">
    <a-button type="primary" @click="openPopup" class="mr-2"> Mở popup </a-button>
  </div>
  <!-- Component popup tùy chỉnh -->
  <BasicModal @register="registerModal" title="Biểu mẫu lớp popup">
    <!-- Biểu mẫu tùy chỉnh -->
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { BasicModal } from '/@/components/Modal';
  import { useModal } from '/@/components/Modal';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      label: 'Tên nhân viên',
      field: 'name',
      component: 'Input',
    },
    {
      label: 'Giới tính',
      field: 'sex',
      component: 'Select',
      //Điền thuộc tính của component
      componentProps: {
        options: [
          { label: 'Nam', value: 1 },
          { label: 'Nữ', value: 2 },
          { label: 'Không xác định', value: 3 },
        ],
      },
      //Giá trị mặc định
      defaultValue: 3,
    },
    {
      label: 'Tuổi',
      field: 'age',
      component: 'Input',
    },
    {
      label: 'Thời gian vào làm',
      subLabel: '(Không bắt buộc)',
      field: 'entryTime',
      component: 'TimePicker',
      // Tình huống popup: gắn panel dropdown vào body để tránh bị biên của modal cắt mất/Che khuất
      componentProps: {
        getPopupContainer: () => document.body,
      },
    },
  ];

  //BasicModalĐăng ký liên kết;
  const [registerModal, { openModal }] = useModal();

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm, { validate, resetFields }] = useForm({
    schemas: formSchemas,
    // Chiều rộng popup có hạn, đặt cho label một chiều rộng cố định, tránh "Thời gian vào làm (tùy chọn)" bị cắt ngắn
    labelWidth: 120,
    //Ẩn nút thao tác
    showActionButtonGroup: false,
  });

  /**
   * Mở popup
   */
  async function openPopup() {
    //Xem chi tiết trong module BasicModal
    openModal(true, {});
  }
</script>

<style scoped>
  /** Kiểu ô nhập thời gian và số */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }
</style>
