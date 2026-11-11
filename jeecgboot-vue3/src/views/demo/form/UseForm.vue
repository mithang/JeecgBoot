<template>
  <PageWrapper title="UseFormVí dụ thao tác">
    <div class="mb-4">
      <a-button @click="setProps({ labelWidth: 150 })" class="mr-2"> Thay đổilabelWidth </a-button>
      <a-button @click="setProps({ labelWidth: 120 })" class="mr-2"> Khôi phụclabelWidth </a-button>
      <a-button @click="setProps({ size: 'large' })" class="mr-2"> Thay đổiSize </a-button>
      <a-button @click="setProps({ size: 'default' })" class="mr-2"> Khôi phụcSize </a-button>
      <a-button @click="setProps({ disabled: true })" class="mr-2"> Vô hiệu hóa biểu mẫu </a-button>
      <a-button @click="setProps({ disabled: false })" class="mr-2"> Bỏ vô hiệu hóa </a-button>
      <a-button @click="setProps({ compact: true })" class="mr-2"> Biểu mẫu gọn </a-button>
      <a-button @click="setProps({ compact: false })" class="mr-2"> Khôi phục khoảng cách bình thường </a-button>
      <a-button @click="setProps({ actionColOptions: { span: 8 } })" class="mr-2"> Vị trí nút thao tác </a-button>
    </div>
    <div class="mb-4">
      <a-button @click="setProps({ showActionButtonGroup: false })" class="mr-2"> Ẩn nút thao tác </a-button>
      <a-button @click="setProps({ showActionButtonGroup: true })" class="mr-2"> Hiển thị nút thao tác </a-button>
      <a-button @click="setProps({ showResetButton: false })" class="mr-2"> Ẩn nút đặt lại </a-button>
      <a-button @click="setProps({ showResetButton: true })" class="mr-2"> Hiển thị nút đặt lại </a-button>
      <a-button @click="setProps({ showSubmitButton: false })" class="mr-2"> Ẩn nút tìm kiếm </a-button>
      <a-button @click="setProps({ showSubmitButton: true })" class="mr-2"> Hiển thị nút tìm kiếm </a-button>
      <a-button
        @click="
          setProps({
            resetButtonOptions: {
              disabled: true,
              text: 'Đặt lạiNew',
            },
          })
        "
        class="mr-2"
      >
        Sửa nút đặt lại
      </a-button>
      <a-button
        @click="
          setProps({
            submitButtonOptions: {
              disabled: true,
              loading: true,
            },
          })
        "
        class="mr-2"
      >
        Sửa nút tìm kiếm
      </a-button>
    </div>
    <CollapseContainer title="useFormVí dụ">
      <BasicForm @register="register" @submit="handleSubmit" />
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container/index';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { PageWrapper } from '/@/components/Page';

  const schemas: FormSchema[] = [
    {
      field: 'field1',
      component: 'Input',
      label: 'Trường1',
      colProps: {
        span: 8,
      },
      componentProps: {
        placeholder: 'Tùy chỉnhplaceholder',
        onChange: (e: any) => {
          console.log(e);
        },
      },
    },
    {
      field: 'field2',
      component: 'Input',
      label: 'Trường2',
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field3',
      component: 'DatePicker',
      label: 'Trường3',
      colProps: {
        span: 8,
      },
    },
    {
      field: 'fieldTime',
      component: 'RangePicker',
      label: 'Trường thời gian',
      defaultValue: [new Date("2024-03-21"), new Date("2024-03-27")],
      componentProps: {
        valueType: 'Date',
      },
      colProps: {
        span: 8,
      },
    },
    {
      field: 'field4',
      component: 'Select',
      label: 'Trường4',
      colProps: {
        span: 8,
      },
      componentProps: {
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
    },
  ];

  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper },
    setup() {
      const { createMessage } = useMessage();

      const [register, { setProps }] = useForm({
        labelWidth: 120,
        schemas,
        actionColOptions: {
          span: 24,
        },
        fieldMapToTime: [['fieldTime', ['startTime', 'endTime'], 'YYYY-MM']],
      });
      return {
        register,
        schemas,
        handleSubmit: (values: Recordable) => {
          createMessage.success('click search,values:' + JSON.stringify(values));
        },
        setProps,
      };
    },
  });
</script>
