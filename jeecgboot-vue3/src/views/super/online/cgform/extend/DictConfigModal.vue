<template>
  <BasicModal
    wrapClassName="dict-config-modal"
    v-bind="$attrs"
    title="Cấu hình từ điển"
    :width="560"
    @register="registerModal"
    keyboard
    :canFullscreen="false"
    cancelText="Đóng"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" />
    <div class="dict-config-tip">
      <p>· <b>Từ điển hệ thống</b>: chỉ điền Code(ví dụ như <code>sex</code>）</p>
      <p>· <b>Từ điển bảng</b>：Table=Tên bảng,Code=trường lưu trữ giá trị,Text=Trường hiển thị; có điều kiện thì có thể ở Table thêm vào sau <code>where ...</code></p>
      <p>· <b>Popup Popup</b>：Table=Báo cáo code，Code=Trường báo cáo,Text=Trường điền ngược của bảng này, nhiều trường dùng dấu phẩy</p>
      <p>· <b>Popup từ điển</b>：Table=Báo cáo code，Code/Text mỗi cái một trường</p>
      <p>· <b>Cây tùy chỉnh</b>：Text=<code>id,pid,Trường hiển thị,Có trường nút con</code></p>
      <p>· <b>Dropdown liên động</b>：Table điền JSON chuỗi,Code/Text Để trống</p>
      <p>· <b>bản ghi liên kết</b>：Table=Tên bảng liên kết,Code=trường lưu trữ giá trị (thường là <code>id</code>），Text=Trường tiêu đề,Trường hiển thị khác (ví dụ <code>name,age,sex</code>, cái đầu tiên là trường tiêu đề)</p>
      <p>· <b>trường bảng khác</b>：Table=Tên trường bản ghi liên kết của bảng này,Code Để trống,Text=Trường cần hiển thị trong bảng liên kết (ví dụ <code>name</code>）</p>
    </div>
  </BasicModal>
</template>

<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form/index';

  export default defineComponent({
    name: 'DictConfigModal',
    components: {
      BasicModal,
      BasicForm,
    },
    emits: ['success', 'register'],
    setup(_p, { emit }) {
      const rowKey = ref('');
      const fieldShowType = ref('');

      const formSchemas: any[] = [
        {
          label: 'từ điểnTable',
          field: 'dictTable',
          component: 'InputTextArea',
          componentProps: {
            placeholder: '',
            rows: 3,
            class: 'dict-config-table-input',
            allowClear: true,
          },
        },
        {
          label: 'từ điểnCode',
          field: 'dictField',
          component: 'Input',
          componentProps: {
            placeholder: '',
            allowClear: true,
          },
          rules: [
            {
              validator: (_, value) => {
                if (fieldShowType.value === 'popup_dict' && value && value.indexOf(',') !== -1) {
                  return Promise.reject('popupComponent từ điển chỉ cho phép điền một trường');
                }
                return Promise.resolve();
              },
              trigger: 'blur',
            },
          ],
        },
        {
          label: 'từ điểnText',
          field: 'dictText',
          component: 'Input',
          componentProps: {
            placeholder: '',
            allowClear: true,
          },
          rules: [
            {
              validator: (_, value) => {
                if (fieldShowType.value === 'popup_dict' && value && value.indexOf(',') !== -1) {
                  return Promise.reject('popupComponent từ điển chỉ cho phép điền một trường');
                }
                return Promise.resolve();
              },
              trigger: 'blur',
            },
          ],
        },
      ];

      const [registerForm, { validate, setFieldsValue, resetFields }] = useForm({
        schemas: formSchemas,
        showActionButtonGroup: false,
        labelAlign: 'right',
        labelWidth: 90,
      });

      const [registerModal, { closeModal }] = useModalInner(async (data) => {
        rowKey.value = data?.rowKey ?? '';
        fieldShowType.value = data?.fieldShowType ?? '';
        await resetFields();
        await setFieldsValue({
          dictTable: data?.dictTable ?? '',
          dictField: data?.dictField ?? '',
          dictText: data?.dictText ?? '',
        });
      });

      async function handleSubmit() {
        const data = await validate();
        emit('success', {
          rowKey: rowKey.value,
          dictTable: (data.dictTable ?? '').trim(),
          dictField: (data.dictField ?? '').trim(),
          dictText: (data.dictText ?? '').trim(),
        });
        closeModal();
      }

      return {
        registerModal,
        registerForm,
        handleSubmit,
      };
    },
  });
</script>

<style lang="less" scoped>
  :deep(.dict-config-table-input) {
    min-height: 64px;
    max-height: 240px !important;
    resize: vertical;
  }

  .dict-config-tip {
    margin-top: 4px;
    padding: 8px 12px;
    background-color: #fafafa;
    border-radius: 4px;
    color: rgba(0, 0, 0, 0.55);
    font-size: 12px;
    line-height: 1.7;
    p {
      margin: 0;
    }
    b {
      color: rgba(0, 0, 0, 0.75);
      font-weight: 600;
    }
    code {
      padding: 0 4px;
      margin: 0 1px;
      background-color: rgba(0, 0, 0, 0.06);
      border-radius: 3px;
      font-family: SFMono-Regular, Consolas, monospace;
      font-size: 11px;
      color: #c7254e;
    }
    u {
      text-decoration: none;
      color: #d4380d;
      font-weight: 500;
    }
  }
</style>
