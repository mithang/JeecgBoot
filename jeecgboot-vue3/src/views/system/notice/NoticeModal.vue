<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    @ok="handleSubmit"
    :title="title"
    width="900px"
    wrapClassName="notice-cls-modal"
    :maxHeight="800"
    destroyOnClose
  >
    <BasicForm @register="registerForm">
      <template #msgTemplate="{ model, field }">
        <a-select v-model:value="model[field]" placeholder="Vui lòng chọn mẫu tin nhắn" :options="templateOption" @change="handleChange" />
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { formSchema } from './notice.data';
  import { getTempList, saveOrUpdate } from './notice.api';
  // Khai báoEmits
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const record = ref<any>({});
  const templateOption = ref([]);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    labelWidth: 100,
    baseRowStyle: { marginTop: '10px' },
    baseColProps: { xs: 24, sm: 12, md: 12, lg: 12, xl: 12, xxl: 12 },
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Tải mẫu
    await initTemplate();
    //Đặt lại biểu mẫu
    await resetFields();
    setModalProps({ confirmLoading: false });
    isUpdate.value = !!data?.isUpdate;
    if (unref(isUpdate)) {
      if (data.record.userIds) {
        data.record.userIds = data.record.userIds.substring(0, data.record.userIds.length - 1);
      }
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
      record.value = data.record;
    } else {
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
  });
  //Đặt tiêu đề
  const title = computed(() => (!unref(isUpdate) ? 'Thêm mới' : 'Chỉnh sửa'));
  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      // Giải thích logic mã: [issue#429]Thêm mới thông báo: tham số người dùng chỉ định khi gửi cóundefined ---
      if(values.msgType==='ALL'){
        values.userIds = '';
      }else{
        values.userIds += ',';
      }
      if (isUpdate.value && record.value.sendStatus != '2') {
        values.sendStatus = '0';
      }
      await saveOrUpdate(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Làm mới danh sách (mang dữ liệu biểu mẫu ra ngoài, trang danh sách cần tự động chuyển điều kiện truy vấn theo loại tin nhắn)
      emit('success', values);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
  //Khởi tạo mẫu
  async function initTemplate() {
    const res = await getTempList({ templateCategory: 'notice', pageSize: 100 });
    console.log('res', res);
    if (res.records && res.records.length > 0) {
      templateOption.value = res.records.map((item) => {
        return {
          label: item.templateName,
          value: item.templateCode,
          content: item.templateContent,
        };
      });
    }
  }

  /**
   * Sửa mẫu
   * @param val
   */
  function handleChange(val) {
    const content = templateOption.value.find((item: any) => item.value === val)?.content;
    if (content) {
      setFieldsValue({
        msgContent: content,
      });
    }
  }
</script>
<style scoped>
  .notice-cls-modal {
    top: 20px !important;
  }
</style>
