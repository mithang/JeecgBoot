<!-- Thêm/bớt biểu mẫu động -->
<template>
  <!-- Biểu mẫu tùy chỉnh -->
  <BasicForm @register="registerForm" style="margin-top: 20px;" @submit="handleSubmit">
    <!--  Thêm slot của input  -->
    <template #addForm="{ field }">
      <a-button v-if="Number(field) === 0" @click="addField" style="width: 50px">+</a-button>
      <a-button v-if="Number(field) > 0" @click="delField(field)" style="width: 50px">-</a-button>
    </template>
  </BasicForm>
  <!--  <div style="margin: 20px auto; text-align: center">
    <a-button @click="addField">Thêm mục biểu mẫu</a-button>
  </div>-->
</template>

<script lang="ts" setup>
  //Nhập dependency
  import { useForm, BasicForm, FormSchema } from '/@/components/Form';
  import { CollapseContainer } from '/@/components/Container';
  import { ref } from 'vue';

  //Trường biểu mẫu tùy chỉnh
  const formSchemas: FormSchema[] = [
    {
      field: 'name1',
      label: 'Họ tên1',
      component: 'Input',
      // ifShow:false,
      colProps: {
        span: 8,
      },
    },
    {
      field: 'age1',
      label: 'Tuổi1',
      component: 'InputNumber',
      // ifShow:false,
      colProps: {
        span: 8,
      },
    },
    {
      field: '0',
      component: 'Input',
      // ifShow:false,
      label: ' ',
      colProps: {
        span: 8,
      },
      slot: 'addForm',
    },
  ];

  /**
   * BasicFormĐăng ký liên kết;
   * appendSchemaByField:Thêm mục biểu mẫu (trường)
   *
   * removeSchemaByFiled:Giảm mục biểu mẫu (trường)
   */
  const [registerForm, { appendSchemaByField, removeSchemaByFiled }] = useForm({
    schemas: formSchemas,
    showResetButton: false,
    labelWidth: '150px',
    // showSubmitButton:false
    submitButtonOptions: { text: 'Gửi', preIcon: '' },
    actionColOptions: { span: 17 },
  });

  //Số lượng component
  let n = ref<number>(2);

  /**
   * Thêm trường
   * appendSchemaByFieldLoại: ( schema: FormSchema, prefixField: string | undefined, first?: boolean | undefined ) => Promise<void>
   * Giải thích: Chèn vào sau filed đã chỉ định, nếu không truyền field đã chỉ định thì chèn vào cuối,Khi first = true sẽ chèn vào vị trí đầu tiên
   */
  async function addField() {
    //Thêm trường biểu mẫu, bên trong là thuộc tính tương ứng của schemas, có thể tự cấu hình
    await appendSchemaByField(
      {
        field: `name${n.value}`,
        component: 'Input',
        label: 'Trường' + n.value,
        colProps: {
          span: 8,
        },
      },
      ''
    );
    await appendSchemaByField(
      {
        field: `sex${n.value}`,
        component: 'InputNumber',
        label: 'Trường' + n.value,
        colProps: {
          span: 8,
        },
      },
      ''
    );

    await appendSchemaByField(
      {
        field: `${n.value}`,
        component: 'Input',
        label: ' ',
        colProps: {
          span: 8,
        },
        slot: 'addForm',
      },
      ''
    );
    n.value++;
  }

  /**
   * Xóa trường
   * Loại: (field: string | string[]) => Promise<void>
   * Giải thích: Xóa theo field Schema
   * @param field Tên trường hiện tại
   */
  function delField(field) {
    //Xóa trường được chỉ định
    removeSchemaByFiled([`name${field}`, `sex${field}`, `${field}`]);
    n.value--;
  }

  /**
   * Giá trị value của nút gửi khi nhấp
   * @param values
   */
  function handleSubmit(values: any) {
    console.log('Dữ liệu nút gửi::::', values);
  }
</script>

<style scoped>
  /** Kiểu ô nhập số */
  :deep(.ant-input-number) {
    width: 100%;
  }
</style>
