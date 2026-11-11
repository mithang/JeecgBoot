<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="800px" destroyOnClose>
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup name="tenant-pack-menu-modal">
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { BasicForm, useForm } from '/@/components/Form';
  import { packMenuFormSchema } from '../tenant.data';
  import { addPackPermission, editPackPermission } from '../tenant.api';

  const isUpdate = ref<boolean>(false);
  // EmitsKhai báo
  const emit = defineEmits(['register', 'success']);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate, setProps }] = useForm({
    schemas: packMenuFormSchema,
    showActionButtonGroup: false,
  });
  //Đơn vị thuê (tenant)
  const tenantId = ref<number>();
  //Loại gói dịch vụ
  const packType = ref<number>();
  //Quyền hạn
  const permissionIds = ref<string>();
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    isUpdate.value = !!data?.isUpdate;
    if(data.tenantId){
      tenantId.value = data.tenantId;
    }
    packType.value = data.packType;
    if (unref(isUpdate)) {
      //Gán giá trị cho biểu mẫu
      console.log(data.record)
      await setFieldsValue({ ...data.record });
      permissionIds.value = data.record.permissionIds;
    }
    // Giải thích logic mã: 【QQYUN-5685】2 Gói dịch vụ bổ sung một mục xem: thêm kiểm tra phía dưới có nút hay không và vô hiệu hóa biểu mẫu------------
    setModalProps({ confirmLoading: false, showCancelBtn:!!data?.showFooter, showOkBtn:!!data?.showFooter });
    // Khi ẩn phần chân thì vô hiệu hóa toàn bộ biểu mẫu
    setProps({ disabled: !data?.showFooter })
  });
  //Đặt tiêu đề
  const title = computed(() => (unref(isUpdate) ? 'Chỉnh sửa gói đơn vị thuê' : 'Thêm mới gói đơn vị thuê'));
  //Sự kiện gửi biểu mẫu
  async function handleSubmit(v) {
    const values = await validate();
    
    setModalProps({ confirmLoading: true });
    values.packType = unref(packType);
    if(values.packType === 'custom'){
      values.tenantId = unref(tenantId);
    }else{
      values.tenantId = 0;
    }
    values.permissionIds = permissionIds.value;
    if (!unref(isUpdate)) {
      await addPackPermission(values);
    } else {
      await editPackPermission(values);
    }
    emit('success');
    setModalProps({ confirmLoading: false });
    closeModal();
  }
</script>
