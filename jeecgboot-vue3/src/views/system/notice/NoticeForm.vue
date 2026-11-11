<template>
  <div style="min-height: 400px">
    <BasicForm @register="registerForm">
      <template #msgTemplate="{ model, field }">
        <a-select v-model:value="model[field]" placeholder="Vui lòng chọn mẫu tin nhắn" :options="templateOption" @change="handleChange" />
      </template>
      <template #msgContent="{ model, field }">
        <div v-html="model[field]" class="article-content"></div>
      </template>
    </BasicForm>
    <div class="footer-btn" v-if="!formDisabled">
      <a-button @click="submitForm" pre-icon="ant-design:check" type="primary">Gửi</a-button>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { getBpmFormSchema } from './notice.data';
  import { getTempList, queryById, saveOrUpdate } from './notice.api';
  import { computed, ref } from 'vue';
  // Định nghĩa thuộc tính
  const props = defineProps({
    formData: {
      type: Object,
      default: () => ({}),
    },
  });
  //Vô hiệu hóa biểu mẫu
  const formDisabled = computed(() => {
    if (props.formData.disabled === false) {
      return false;
    }
    return true;
  });
  const templateOption = ref([]);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    schemas: getBpmFormSchema(props.formData),
    showActionButtonGroup: false,
    disabled: formDisabled.value,
    labelWidth: 100,
    baseRowStyle: { marginTop: '10px' },
    baseColProps: { xs: 24, sm: 12, md: 12, lg: 12, xl: 12, xxl: 12 },
  });

  //Gửi biểu mẫu
  async function submitForm() {
    let values = await validate();
    if (values.msgType === 'ALL') {
      values.userIds = '';
    } else {
      values.userIds += ',';
    }
    console.log('Dữ liệu biểu mẫu', values);
    await saveOrUpdate(values, true);
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
  /**
   * Tải dữ liệu
   */
  async function initFormData() {
    let res = await queryById({ id: props.formData.dataId });
    if (res.success) {
      //Đặt lại biểu mẫu
      await resetFields();
      const record = res.result;
      if (record.userIds) {
        record.userIds = record.userIds.substring(0, record.userIds.length - 1);
      }
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...record,
      });
    }
  }
  //Tải mẫu
  initTemplate();
  //Tải dữ liệu
  initFormData();
</script>
<style lang="less" scoped>
  .footer-btn {
    width: 100%;
    text-align: center;
  }
  .article-content {
    max-width: 100%;
    max-height: 500px;
    overflow-y: auto;
  }
</style>
