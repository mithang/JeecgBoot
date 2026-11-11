<template>
  <PageWrapper title="Ví dụ biểu mẫu có thể thu gọn">
    <CollapseContainer title="Ví dụ thu gọn cơ bản">
      <BasicForm @register="register" />
    </CollapseContainer>

    <CollapseContainer title="Vượt quá 5 cột tự động thu gọn, khi gấp giữ lại 2 dòng" class="mt-4">
      <BasicForm @register="register1" />
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container';
  import { PageWrapper } from '/@/components/Page';

  const getSchamas = (): FormSchema[] => {
    return [
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
      // {
      //   field: 'field7',
      //   component: 'RadioGroup',
      //   label: 'Trường7',
      //   colProps: {
      //     span: 8,
      //   },
      //   componentProps: {
      //     options: [
      //       {
      //         label: 'Tùy chọn1',
      //         value: '1',
      //       },
      //       {
      //         label: 'Tùy chọn2',
      //         value: '2',
      //       },
      //     ],
      //   },
      // },
    ];
  };

  function getAppendSchemas(): FormSchema[] {
    return [
      {
        field: 'field10',
        component: 'Input',
        label: 'Trường10',
        colProps: {
          span: 8,
        },
      },
      {
        field: 'field11',
        component: 'Input',
        label: 'Trường11',
        colProps: {
          span: 8,
        },
      },
      {
        field: 'field12',
        component: 'Input',
        label: 'Trường12',
        colProps: {
          span: 8,
        },
      },
      {
        field: 'field13',
        component: 'Input',
        label: 'Trường13',
        colProps: {
          span: 8,
        },
      },
    ];
  }
  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper },
    setup() {
      const [register] = useForm({
        labelWidth: 120,
        schemas: getSchamas(),
        actionColOptions: {
          span: 24,
        },
        compact: true,
        showAdvancedButton: true,
      });
      const extraSchemas: FormSchema[] = [];
      for (let i = 14; i < 30; i++) {
        extraSchemas.push({
          field: 'field' + i,
          component: 'Input',
          label: 'Trường' + i,
          colProps: {
            span: 8,
          },
        });
      }
      const [register1] = useForm({
        labelWidth: 120,
        schemas: [...getSchamas(), ...getAppendSchemas(), { field: '', component: 'Divider', label: 'Thêm trường' }, ...extraSchemas],
        actionColOptions: {
          span: 8,
        },
        compact: true,
        showAdvancedButton: true,
        autoAdvancedCol: 2,
        alwaysShowLines: 1,
      });
      return {
        register,
        register1,
      };
    },
  });
</script>
