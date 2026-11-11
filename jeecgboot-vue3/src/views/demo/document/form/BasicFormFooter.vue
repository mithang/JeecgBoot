<!-- Chân trang tùy chỉnh -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px">
    <template #formHeader>
      <div style="margin: 0 auto 20px">
        <span>Tôi là nút tùy chỉnh</span>
      </div>
    </template>
    <template #formFooter>
      <div style="margin: 0 auto">
        <a-button type="primary" @click="save" class="mr-2"> Lưu </a-button>
        <a-button type="primary" @click="saveDraft" class="mr-2"> Lưu bản nháp </a-button>
        <a-button type="error" @click="reset" class="mr-2"> Đặt lại </a-button>
      </div>
    </template>
  </BasicForm>
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';

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
      subLabel: '( Không bắt buộc )',
      field: 'entryTime',
      component: 'TimePicker',
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm, { validate, resetFields }] = useForm({
    schemas: formSchemas,
    labelWidth: '150px',
    //Ẩn nút thao tác
    showActionButtonGroup: false,
  });

  /**
   * Lưu
   */
  async function save() {
    //Dùng phương thức useForm để lấy giá trị biểu mẫu
    let values = await validate();
    console.log(values);
  }

  /**
   * Lưu bản nháp
   */
  async function saveDraft() {
    //Sử dụng phương thức validate của useForm để lấy giá trị biểu mẫu
    let values = await validate();
    console.log(values);
  }

  /**
   * Đặt lại
   */
  async function reset() {
    //Dùng phương thức useForm resetFields để xóa giá trị
    await resetFields();
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
