<!-- Bố cục biểu mẫu -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px" />
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      label: 'Tên cuộc họp',
      field: 'name',
      component: 'Input',
    },
    {
      label: 'Địa điểm họp',
      field: 'meetingLocation',
      component: 'Input',
    },
    {
      label: 'Số người tham gia',
      field: 'numberOfPart',
      component: 'InputNumber',
    },
    {
      label: 'Biên bản cuộc họp',
      field: 'meetingMinutes',
      component: 'JUpload',
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm] = useForm({
    //Thuộc tính bố cục biểu mẫu, hỗ trợ (vertical,inline), mặc định làinline
    layout: 'inline',
    //Đăng ký cột biểu mẫu
    schemas: formSchemas,
    //Không hiển thị nút tìm kiếm và nút đặt lại
    showActionButtonGroup: false,
    //Cấu hình hàng row mặc định,Có hiệu lực khi layout là inline
    rowProps: { gutter: [24, 24], justify: 'center', align: 'middle' },
    //Tỷ lệ chiếm của cột col toàn cục(Mỗi cột hiển thị bao nhiêu vị trí), giống với thuộc tính colProps trong schemas
    baseColProps: { span: 12 },
    //rowKiểu dáng của dòng
    baseRowStyle: { width: '100%' },
  });
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
