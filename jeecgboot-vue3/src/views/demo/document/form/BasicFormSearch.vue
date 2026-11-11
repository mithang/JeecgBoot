<!-- Khu vực tìm kiếm -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" @submit="handleSubmit" style="margin-top: 20px" />
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'name',
      label: 'Họ tên',
      component: 'Input',
    },
    {
      field: 'hobby',
      label: 'Sở thích',
      component: 'Input',
    },
    {
      field: 'birthday',
      label: 'Ngày sinh',
      component: 'DatePicker',
    },
    {
      field: 'joinTime',
      label: 'Thời gian vào làm',
      component: 'RangePicker',
      componentProps: {
        valueType: 'Date',
      },
    },
    {
      field: 'workYears',
      label: 'Số năm làm việc',
      component: 'JRangeNumber',
    },
    {
      field: 'sex',
      label: 'Giới tính',
      component: 'Select',
      componentProps: {
        options: [
          {
            label: 'Nam',
            value: '1',
          },
          {
            label: 'Nữ',
            value: '2',
          },
        ],
      },
    },
    {
      field: 'marital',
      label: 'Tình trạng hôn nhân',
      component: 'RadioGroup',
      componentProps: {
        options: [
          {
            label: 'Chưa kết hôn',
            value: '1',
          },
          {
            label: 'Đã kết hôn',
            value: '2',
          },
        ],
      },
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm] = useForm({
    //Ánh xạ giá trị vùng thời gian trong biểu mẫu thành 2 trường, 'YYYY-MM-DD'Định dạng ngày
    fieldMapToTime: [['joinTime', ['joinTime_begin', 'joinTime_end'], 'YYYY-MM-DD']],
    //Đăng ký cột biểu mẫu
    schemas: formSchemas,
    //Có hiển thị nút mở rộng/thu gọn hay không, mặc địnhfalse
    showAdvancedButton: true,
    //Vượt quá số dòng chỉ định thì thu gọn, mặc định 3 dòng
    autoAdvancedCol: 3,
    //Số dòng hiển thị mặc định khi thu gọn, mặc định 1 dòng
    alwaysShowLines: 2,

    //Ánh xạ giá trị của vùng kiểu số trong biểu mẫu thành 2 trường
    fieldMapToNumber: [['workYears', ['workYears_begin', 'workYears_end']]],
    //Tỷ lệ mỗi cột chiếm, mặc định một hàng là24
    baseColProps: { span: 12 },
  });

  /**
   * Giá trị value của nút gửi khi nhấp
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('Dữ liệu nút gửi::::', values);
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
