<template>
  <ConfigProvider :theme="{ token: { fontSize: 13, controlHeight: 28 } }">
    <BasicModal
      @register="registerModal"
      title="Mục cấu hình mở rộng biểu mẫu"
      :width="900"
      :bodyStyle="{ padding: '24px 28px 12px' }"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <BasicForm class="onl-cgform-ext-config-form" @register="registerForm" />
    </BasicModal>
  </ConfigProvider>
</template>

<script lang="ts">
  import { ConfigProvider } from 'ant-design-vue';
  import { nextTick, defineComponent } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useExtendConfigFormSchemas } from '../hooks/useSchemas';

  export default defineComponent({
    name: 'CgformExtConfigModel',
    components: { BasicModal, BasicForm, ConfigProvider },
    props: {
      parentForm: {
        type: Object,
        required: true,
      },
    },
    emits: ['register', 'ok'],
    setup(props, { emit }) {
      const { createMessage: $message } = useMessage();

      const { formSchemas } = useExtendConfigFormSchemas(props, {
        onIsDesformChange,
        onJoinQueryChange,
        onReportPrintShowChange,
        onFormLabelLengthShow,
        // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu
        onEnableMultiDataSourceChange,
        // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu
      });

      // Cấu hình biểu mẫu
      const [registerForm, { resetFields, setFieldsValue, getFieldsValue, clearValidate, validate }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
      });

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        await resetFields();
        await setFieldsValue(data.extConfigJson);
      });

      async function handleOk() {
        await clearValidate();
        await nextTick();
        try {
          const values = await validate();
          emit('ok', values);
          closeModal();
        } catch (e) {}
      }

      function handleCancel() {
        closeModal();
      }

      // Sự kiện thay đổi thiết kế biểu mẫu khi kết nối
      function onIsDesformChange(value) {
        if (value === 'Y') {
          let { themeTemplate } = props.parentForm.getFieldsValue(['themeTemplate']);
          if ('erp' === themeTemplate) {
            props.parentForm.setFieldsValue({ themeTemplate: 'normal' });
            $message.warning('Xin lưu ý:erpPhong cách không hỗ trợ kết nối thiết kế biểu mẫu, đã tự động đổi thành phong cách mặc định!');
          }
        } else {
          clearValidate('desFormCode');
        }
      }

      // Mặc định nhắc địa chỉ báo cáo Jimu
      const defaultReportPrintUrl = `{{ window._CONFIG['domianURL'] }}/jmreport/view/{Báo cáo JimuID}`;

      // Sự kiện thay đổi tích hợp báo cáo Jimu
      async function onReportPrintShowChange(value) {
        let reportPrintUrl = getFieldsValue()['reportPrintUrl'];
        // 0 = Khi đóng, xóa địa chỉ nhắc nhở mặc định
        if (value === 0) {
          if (reportPrintUrl === defaultReportPrintUrl) {
            await setFieldsValue({ reportPrintUrl: '' });
          }
        } else if (value === 1) {
          if (reportPrintUrl === '') {
            await setFieldsValue({ reportPrintUrl: defaultReportPrintUrl });
          }
        }
        clearValidate('reportPrintUrl');
      }
      /**
       * 2024-03-29
       * liaozhiyang
       * Thống nhất biểu mẫulabelKhi độ dài bị đóng,formLabelLengthĐặt thành trống
       */
      async function onFormLabelLengthShow(value) {
        if (value == 0) {
          await setFieldsValue({ formLabelLength: null });
        }
        await clearValidate('formLabelLength');
      }

      // update-begin--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu: khi tắt đa nguồn dữ liệu, xóa trống nguồn dữ liệu đã chọn
      async function onEnableMultiDataSourceChange(value) {
        if (value == 0) {
          await setFieldsValue({ dbSource: '' });
        }
        await clearValidate('dbSource');
      }
      // update-end--author:jeecg---date:20260512---for：【QQYUN-15337】onlineBiểu mẫu hỗ trợ đa nguồn dữ liệu

      // Sự kiện thay đổi truy vấn liên hợp
      function onJoinQueryChange(value) {
        if (value === 1) {
          let { themeTemplate, isTree, tableType } = props.parentForm.getFieldsValue(['themeTemplate', 'isTree', 'tableType']);
          if ('erp' === themeTemplate) {
            $message.warning('Xin lưu ý:erpPhong cách không hỗ trợ truy vấn liên hợp, cấu hình không có hiệu lực!');
            setFieldsValue({ joinQuery: 0 });
          }
          if ('innerTable' === themeTemplate) {
            $message.warning('Xin lưu ý: phong cách nhúng không hỗ trợ truy vấn liên hợp, cấu hình không có hiệu lực!');
            setFieldsValue({ joinQuery: 0 });
          }
          if (1 === tableType) {
            $message.warning('Xin lưu ý: bảng đơn không hỗ trợ truy vấn liên hợp, cấu hình không có hiệu lực!');
            setFieldsValue({ joinQuery: 0 });
          } else if (3 === tableType) {
            $message.warning('Xin lưu ý: bảng hiện tại là bảng phụ, vui lòng cấu hình ở bảng chính tương ứng!');
            setFieldsValue({ joinQuery: 0 });
          } else if ('Y' === isTree) {
            $message.warning('Xin lưu ý: danh sách dạng cây không hỗ trợ truy vấn liên hợp, cấu hình không có hiệu lực!');
            setFieldsValue({ joinQuery: 0 });
          }
        }
      }

      return {
        handleOk,
        handleCancel,
        registerModal,
        registerForm,
      };
    },
  });
</script>

<style scoped lang="less">
  .onl-cgform-ext-config-form {
    :deep(.ant-form-item) {
      margin-bottom: 14px;
    }

    :deep(.image-export-mode .ant-radio-button-wrapper) {
      padding-inline: 8px;
    }
  }
</style>
