<template>
  <PageWrapper title="Ví dụ component tùy chỉnh">
    <CollapseContainer title="Biểu mẫu tùy chỉnh">
      <BasicForm @register="register" @submit="handleSubmit">
        <template #f3="{ model, field }">
          <a-input v-model:value="model[field]" placeholder="Tùy chỉnhslot" />
        </template>
      </BasicForm>
    </CollapseContainer>
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, h } from 'vue';
  import { BasicForm, FormSchema, useForm } from '/@/components/Form/index';
  import { CollapseContainer } from '/@/components/Container/index';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { Input } from 'ant-design-vue';
  import { PageWrapper } from '/@/components/Page';

  const schemas: FormSchema[] = [
    {
      field: 'field1',
      component: 'Input',
      label: 'renderCách thức',
      colProps: {
        span: 8,
      },
      rules: [{ required: true }],
      render: ({ model, field }) => {
        return h(Input, {
          placeholder: 'Vui lòng nhập',
          value: model[field],
          onChange: (e: ChangeEvent) => {
            model[field] = e.target.value;
          },
        });
      },
    },
    {
      field: 'field2',
      component: 'Input',
      label: 'renderComponentslot',
      colProps: {
        span: 8,
      },
      rules: [{ required: true }],
      renderComponentContent: () => {
        return {
          suffix: () => 'suffix',
        };
      },
    },
    {
      field: 'field3',
      component: 'Input',
      label: 'Tùy chỉnhSlot',
      slot: 'f3',
      colProps: {
        span: 8,
      },
      rules: [{ required: true }],
    },
  ];
  export default defineComponent({
    components: { BasicForm, CollapseContainer, PageWrapper, [Input.name]: Input },
    setup() {
      const { createMessage } = useMessage();
      const [register, { setProps }] = useForm({
        labelWidth: 120,
        schemas,
        actionColOptions: {
          span: 24,
        },
      });
      return {
        register,
        schemas,
        handleSubmit: (values: any) => {
          createMessage.success('click search,values:' + JSON.stringify(values));
        },
        setProps,
      };
    },
  });
</script>
