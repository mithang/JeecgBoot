<template>
  <div class="p-4">
    <div class="p-4 bg-white">
      <a-button-group class="j-table-operator">
        <a-button type="primary" @click="setDis(0)">Kích hoạt</a-button>
        <a-button type="primary" @click="setDis(1)">Vô hiệu hóa</a-button>
        <a-button type="primary" @click="getValues()">Kiểm chứng biểu mẫu và lấy giá trị</a-button>
        <a-button type="primary" @click="setValues()">Đặt giá trị</a-button>
      </a-button-group>

      <BasicForm @register="register" @submit="handleSubmit" />
    </div>
  </div>
</template>

<script lang="ts">
  export default {
    title: 'Văn bản phong phú | Markdown',
    name: 'MarkdownDemo',
  };
</script>

<script lang="ts" setup>
  import { FormSchema, useForm, BasicForm } from '/@/components/Form';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage, createSuccessModal } = useMessage();

  const schemas: FormSchema[] = [
    {
      field: 'name',
      component: 'Input',
      label: 'Họ tên',
      required: true,
      defaultValue: 'zhangsan',
    },
    {
      field: 'tinymce',
      component: 'JEditor',
      label: 'Văn bản phong phú',
      defaultValue: 'defaultValue',
      required: true,
    },
    {
      field: 'markdown',
      component: 'JMarkdownEditor',
      label: 'Markdown',
      defaultValue: '# Trương Tam',
      required: true,
      componentProps: {
        height: 300,
      },
    },
  ];

  const [register, { setProps, validate, setFieldsValue }] = useForm({
    labelWidth: 120,
    schemas: schemas,
    actionColOptions: {
      span: 24,
    },
    compact: true,
    showResetButton: false,
    showSubmitButton: false,
    showAdvancedButton: false,
    disabled: false,
  });

  function handleSubmit(values) {
    console.log(values);
  }

  function setDis(flag) {
    setProps({ disabled: !!flag });
  }

  async function getValues() {
    try {
      const values = await validate();
      console.log(values);
      createSuccessModal({
        title: 'Kiểm tra đạt',
        content: `${JSON.stringify(values)}`,
      });
    } catch (error) {
      createMessage.warning('Kiểm tra không đạt');
    }
  }

  function setValues() {
    setFieldsValue({
      name: 'LiSi',
      markdown: '# Lý Tứ',
      tinymce: '<p><strong><span style="font-size: 18pt;">Trương<span style="color: #e03e2d;">Tam</span>Phong</span></strong></p>',
    });
  }
</script>

<style scoped></style>
