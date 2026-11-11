<!-- Kết xuất tùy chỉnh -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px">
    <!--  #phonetương ứng với formSchemasslot(phone)Slot    -->
    <template #phone="{ model, field }">
      <!-- Nếu là component cần binding hai chiều, model là đối tượng biểu mẫu hiện tại, field là tên trường hiện tại  -->
      <a-input v-model:value="model[field]" placeholder="Vui lòng nhập số điện thoại" />
      <span class="font-color">Vui lòng nhập số điện thoại của bạn để chúng tôi tiện liên hệ</span>
    </template>
    <template #feedback="{ model, field }">
      <JEditor v-model:value="model[field]" placeholder="Vui lòng nhập phản hồi vấn đề" />
      <span class="font-color">Mong bạn trình bày kèm hình ảnh và chữ, để chúng tôi hiểu vấn đề và phản hồi kịp thời</span>
    </template>
  </BasicForm>
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import JEditor from '/@/components/Form/src/jeecg/components/JEditor.vue';
  import { h } from 'vue';
  import { Input } from 'ant-design-vue';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'productName',
      label: 'Tên sản phẩm',
      component: 'Input',
    },
    {
      field: 'price',
      label: 'Giá',
      component: 'InputNumber',
    },
    {
      field: 'buyNums',
      label: 'Số lượng mua',
      component: 'InputNumber',
      //model Đối tượng biểu mẫu đơn, field là trường hiện tại
      render: ({ model, field }) => {
        //Render component tùy chỉnh, lấy Input làm ví dụ
        return h(Input, {
          placeholder: 'Vui lòng nhập số lượng mua',
          value: model[field],
          style: { width: '100%' },
          type: 'number',
          onChange: (e: ChangeEvent) => {
            model[field] = e.target.value;
          },
        });
      },
    },
    {
      field: 'describe',
      label: 'Mô tả',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
      //Render values: tất cả giá trị của biểu mẫu hiện tại
      render: ({ values }) => {
        let productName = values.productName?values.productName:'';
        let price = values.price ? values.price : 0;
        let buyNums = values.buyNums ? values.buyNums : 0;
        return 'Tên sản phẩm đã mua:' + productName + ', Tổng giá: ' + price * buyNums + 'đồng';
      },
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   */
  const [registerForm] = useForm({
    //Đăng ký cột biểu mẫu
    schemas: formSchemas,
    showResetButton: false,
    labelWidth: '150px',
    submitButtonOptions: { text: 'Gửi', preIcon: '' },
    actionColOptions: { span: 17 },
  });
</script>

<style scoped>
  /** Kiểu ô nhập số */
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
