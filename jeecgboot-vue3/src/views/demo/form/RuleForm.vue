<template>
  <PageWrapper title="Ví dụ kiểm chứng biểu mẫu">
    <div class="mb-4">
      <a-button @click="validateForm" class="mr-2"> Kiểm chứng biểu mẫu thủ công</a-button>
      <a-button @click="resetValidate" class="mr-2"> Xóa thông tin kiểm chứng</a-button>
      <a-button @click="getFormValues" class="mr-2"> Lấy giá trị biểu mẫu</a-button>
      <a-button @click="setFormValues" class="mr-2"> Đặt giá trị biểu mẫu</a-button>
      <a-button @click="resetFields" class="mr-2"> Đặt lại</a-button>
    </div>
    <CollapseContainer title="Kiểm chứng biểu mẫu">
      <BasicForm @register="register" @submit="handleSubmit" />
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';
  import { isAccountExist } from '/@/api/demo/system';

  const schemas: FormSchema[] = [
    {
      field: 'field1',
      component: 'Input',
      label: 'Trường1',
      colProps: {
        span: 8,
      },
      required: true,
    },
    {
      field: 'field2',
      component: 'Input',
      label: 'Trường2',
      colProps: {
        span: 8,
      },
      required: true,
    },
    {
      field: 'id',
      label: 'id',
      required: true,
      defaultValue: 0,
      component: 'InputNumber',
      show: false,
    },
    {
      field: 'field3',
      component: 'DatePicker',
      label: 'Trường3',
      colProps: {
        span: 8,
      },
      required: true,
    },
    {
      field: 'field33',
      component: 'DatePicker',
      label: 'Trường33',
      colProps: {
        span: 8,
      },
      componentProps: {
        valueFormat: 'YYYY-MM-DD',
      },
      rules: [{ required: true, type: 'string' }],
    },
    {
      field: 'field44',
      component: 'InputCountDown',
      label: 'Mã xác thực',
      colProps: {
        span: 8,
      },
      required: true,
    },
    {
      field: 'field4',
      component: 'Select',
      label: 'Trường4',
      colProps: {
        span: 8,
      },
      componentProps: {
        mode: 'multiple',
        options: [
          {
            label: 'Tùy chọn1',
            value: '1',
            key: '1',
          },
          {
            label: 'Tùy chọn2',
            value: '2',
            key: '2',
          },
        ],
      },
      rules: [
        {
          required: true,
          message: 'Vui lòng nhập quy tắc',
          type: 'array',
        },
      ],
    },
    {
      field: 'field441',
      component: 'Input',
      label: 'Kiểm chứng tùy chỉnh',
      colProps: {
        span: 8,
      },
      rules: [
        {
          required: true,
          // @ts-ignore
          validator: async (rule, value) => {
            if (!value) {
              /* eslint-disable-next-line */
              return Promise.reject('Giá trị không được để trống');
            }
            if (value === '1') {
              /* eslint-disable-next-line */
              return Promise.reject('Giá trị không được là1');
            }
            return Promise.resolve();
          },
          trigger: 'change',
        },
      ],
    },
    {
      field: 'field5',
      component: 'CheckboxGroup',
      label: 'Trường5',
      colProps: {
        span: 8,
      },
      componentProps: {
        options: [
          {
            label: 'Tùy chọn1',
            value: '1',
          },
          {
            label: 'Tùy chọn2',
            value: '2',
          },
        ],
      },
      rules: [{ required: true }],
    },
    {
      field: 'field7',
      component: 'RadioGroup',
      label: 'Trường7',
      colProps: {
        span: 8,
      },
      componentProps: {
        options: [
          {
            label: 'Tùy chọn1',
            value: '1',
          },
          {
            label: 'Tùy chọn2',
            value: '2',
          },
        ],
      },
      rules: [{ required: true, message: 'Ghi đè thông tin kiểm chứng được tạo mặc định' }],
    },
    {
      field: 'field8',
      component: 'Input',
      label: 'Xác thực bất đồng bộ backend',
      colProps: {
        span: 8,
      },
      helpMessage: ['Trường này minh họa kiểm tra không đồng bộ', 'Quy tắc cục bộ: Bắt buộc điền', 'Quy tắc backend: Không được chứaadmin'],
      rules: [
        {
          required: true,
          message: 'Vui lòng nhập dữ liệu',
        },
        {
          validator(_, value) {
            return new Promise((resolve, reject) => {
              isAccountExist(value)
                .then(() => resolve())
                .catch((err) => {
                  reject(err.message || 'Kiểm tra thất bại');
                });
            });
          },
        },
      ],
    },
  ];

  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper },
    setup() {
      const { createMessage } = useMessage();
      const [register, { validateFields, clearValidate, getFieldsValue, resetFields, setFieldsValue }] = useForm({
        labelWidth: 120,
        schemas,
        actionColOptions: {
          span: 24,
        },
      });

      async function validateForm() {
        try {
          const res = await validateFields();
          console.log('passing', res);
        } catch (error) {
          console.log('not passing', error);
        }
      }

      async function resetValidate() {
        clearValidate();
      }

      function getFormValues() {
        const values = getFieldsValue();
        createMessage.success('values:' + JSON.stringify(values));
      }

      function setFormValues() {
        setFieldsValue({
          field1: 1111,
          field5: ['1'],
          field7: '1',
          field33: '2020-12-12',
          field3: '2020-12-12',
        });
      }

      return {
        register,
        schemas,
        handleSubmit: (values: any) => {
          createMessage.success('click search,values:' + JSON.stringify(values));
        },
        getFormValues,
        setFormValues,
        validateForm,
        resetValidate,
        resetFields,
      };
    },
  });
</script>
