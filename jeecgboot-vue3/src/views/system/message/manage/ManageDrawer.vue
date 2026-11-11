<template>
  <BasicDrawer @register="registerModal" title="Chi tiết" :width="600" v-bind="$attrs" @ok="closeDrawer">
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { BasicForm, useForm } from '/@/components/Form/index';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { formSchemas } from './manage.data';

  // Khai báo emits
  const emit = defineEmits(['register']);
  // Đăng ký form
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: formSchemas,
    showActionButtonGroup: false,
  });
  // Đăng ký modal
  const [registerModal, { closeDrawer }] = useDrawerInner(async (data) => {
    await resetFields();
    await setFieldsValue({ ...data.record });
  });
</script>
