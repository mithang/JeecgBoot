<template>
  <PageWrapper title="Ví dụ biểu mẫu động">
    <div class="mb-4">
      <a-button @click="changeLabel3" class="mr-2"> Thay đổi trường3label </a-button>
      <a-button @click="changeLabel34" class="mr-2"> Đồng thời thay đổi trường3,4label </a-button>
      <a-button @click="appendField" class="mr-2"> Chèn trường vào sau trường 310 </a-button>
      <a-button @click="deleteField" class="mr-2"> Xóa trường11 </a-button>
    </div>
    <CollapseContainer title="Ví dụ biểu mẫu động,Thay đổi động theo các giá trị khác trong biểu mẫu">
      <BasicForm @register="register" />
    </CollapseContainer>

    <CollapseContainer class="mt-5" title="componentPropsThay đổi động">
      <BasicForm @register="register1" />
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container/index';
  import { PageWrapper } from '/@/components/Page';
  import { usePermission } from '/@/hooks/web/usePermission';
  const { hasPermission } = usePermission();
  const schemas: FormSchema[] = [
    {
      field: 'field5',
      component: 'Switch',
      label: 'Có hiển thị trường không1(cssĐiều khiển)',
      defaultValue: true,
      colProps: {
        span: 12,
      },
      labelWidth: 200,
    },
    {
      field: 'field1',
      component: 'Input',
      label: 'Trường1',
      colProps: {
        span: 12,
      },
      show: ({ values }) => {
        return hasPermission('test001');
      },
    },
    {
      field: 'field6',
      component: 'Switch',
      label: 'Có hiển thị trường không2(domĐiều khiển)',
      defaultValue: true,
      colProps: {
        span: 12,
      },
      labelWidth: 200,
    },
    {
      field: 'field2',
      component: 'Input',
      label: 'Trường2',
      colProps: {
        span: 12,
      },
      ifShow: ({ values }) => {
        return !!values.field6;
      },
    },
    {
      field: 'field7',
      component: 'Switch',
      label: 'Có vô hiệu hóa trường không3',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
    },
    {
      field: 'field3',
      component: 'DatePicker',
      label: 'Trường3',
      colProps: {
        span: 12,
      },
      dynamicDisabled: ({ values }) => {
        return !!values.field7;
      },
    },
    {
      field: 'field8',
      component: 'Switch',
      label: 'Trường 4 có bắt buộc không',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
    },
    {
      field: 'field4',
      component: 'Select',
      label: 'Trường4',
      colProps: {
        span: 12,
      },
      dynamicRules: ({ values }) => {
        return values.field8 ? [{ required: true, message: 'Trường bắt buộc' }] : [];
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
      field: 'field11',
      component: 'DatePicker',
      label: 'Trường11',
      colProps: {
        span: 8,
      },
    },
  ];

  const schemas1: FormSchema[] = [
    {
      field: 'f1',
      component: 'Input',
      label: 'F1',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
      componentProps: ({ formModel }) => {
        return {
          placeholder: 'Đồng bộ giá trị của f2 thành f1f1',
          onChange: (e: ChangeEvent) => {
            formModel.f2 = e.target.value;
          },
        };
      },
    },
    {
      field: 'f2',
      component: 'Input',
      label: 'F2',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
      componentProps: { disabled: true },
    },
    {
      field: 'f3',
      component: 'Input',
      label: 'F3',
      colProps: {
        span: 12,
      },
      labelWidth: 200,
      // @ts-ignore
      componentProps: ({ formActionType }) => {
        return {
          placeholder: 'Khi giá trị thay đổi sẽ thực hiện truy vấn,Xem bảng điều khiển (console)',
          onChange: async () => {
            const { validate } = formActionType;
            // tableActionChỉ áp dụng cho ví dụ mở biểu mẫu trong bảng
            // const { reload } = tableAction;
            const res = await validate();
            console.log(res);
          },
        };
      },
    },
  ];

  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper },
    setup() {
      const [register, { setProps, updateSchema, appendSchemaByField, removeSchemaByFiled }] = useForm({
        labelWidth: 120,
        schemas,
        //Vô hiệu hóa tất cả component của biểu mẫu
        disabled: true,
        labelCol: {
          xs: { span: 24 },
          sm: { span: 6 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 18 },
        },
        actionColOptions: {
          span: 24,
        },
      });
      const [register1] = useForm({
        labelWidth: 120,
        schemas: schemas1,
        actionColOptions: {
          span: 24,
        },
      });
      function changeLabel3() {
        updateSchema({
          field: 'field3',
          label: 'Trường3 New',
        });
      }
      function changeLabel34() {
        updateSchema([
          {
            field: 'field3',
            label: 'Trường3 New++',
          },
          {
            field: 'field4',
            label: 'Trường4 New++',
          },
        ]);
      }

      function appendField() {
        appendSchemaByField(
          {
            field: 'field10',
            label: 'Trường10',
            component: 'Input',
            colProps: {
              span: 8,
            },
          },
          'field3'
        );
      }
      function deleteField() {
        removeSchemaByFiled('field11');
      }
      return {
        register,
        register1,
        schemas,
        setProps,
        changeLabel3,
        changeLabel34,
        appendField,
        deleteField,
      };
    },
  });
</script>
