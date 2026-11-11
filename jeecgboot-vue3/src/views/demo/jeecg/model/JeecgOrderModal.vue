<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="700px">
    <BasicForm @register="registerForm" />
    <!--TODO Dữ liệu Tab bảng con-->
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { defHttp } from '/@/utils/http/axios';
  // EmitsKhai báo
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 150,
    schemas: [
      {
        field: 'orderCode',
        label: 'Số đơn hàng',
        component: 'Input',
        required: true,
      },
      {
        field: 'ctype',
        label: 'Loại đơn hàng',
        component: 'Select',
        componentProps: {
          options: [
            { label: 'Đơn hàng nội địa', value: '1' },
            { label: 'Đơn hàng quốc tế', value: '2' },
          ],
        },
      },
      {
        field: 'orderDate',
        label: 'Ngày đặt hàng',
        component: 'DatePicker',
        componentProps: {
          valueFormat: 'YYYY-MM-DD hh:mm:ss',
        },
      },
      {
        field: 'orderMoney',
        label: 'Số tiền đơn hàng',
        component: 'InputNumber',
      },
      {
        field: 'content',
        label: 'Ghi chú đơn hàng',
        component: 'Input',
      },
      {
        field: 'id',
        label: 'id',
        component: 'Input',
        show: false,
      },
    ],
    showActionButtonGroup: false,
  });
  //Gán giá trị biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      //Gán giá trị biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Cài đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới đơn hàng' : 'Chỉnh sửa đơn hàng'));
  //Sự kiện submit biểu mẫu
  async function handleSubmit(v) {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      let url = unref(isUpdate) ? '/test/order/edit' : '/test/order/add';
      defHttp.post({ url: url, params: values });
      //Đóng popup
      closeModal();
      //Làm mới danh sách
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
