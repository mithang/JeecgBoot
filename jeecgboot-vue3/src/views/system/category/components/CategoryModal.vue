<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose width="550px" :title="getTitle" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, computed, unref } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicForm, useForm } from '/src/components/Form';
  import { formSchema } from '../category.data';
  import { loadTreeData, saveOrUpdateDict } from '../category.api';
  // Lấyemit
  const emit = defineEmits(['register', 'success']);
  const isUpdate = ref(true);
  const expandedRowKeys = ref([]);
  const treeData = ref([]);
  const isSubAdd = ref(false);
  //Cấu hình biểu mẫu
  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    schemas: formSchema,
    showActionButtonGroup: false,
    labelCol: {
      xs: { span: 24 },
      sm: { span: 4 },
    },
    wrapperCol: {
      xs: { span: 24 },
      sm: { span: 18 },
    },
  });
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    //Đặt lại biểu mẫu
    await resetFields();
    expandedRowKeys.value = [];
    setModalProps({ confirmLoading: false, minHeight: 80 });
    isUpdate.value = !!data?.isUpdate;
    // Giải thích logic mã: Từ điển phân loại báo lỗi khi data.record trống------------
    isSubAdd.value = !data?.isUpdate && data.record && data.record.id;
    if (data?.record) {
      //Gán giá trị cho biểu mẫu
      await setFieldsValue({
        ...data.record,
      });
    }
    //Thông tin cây của nút cha
    treeData.value = await loadTreeData({ async: false, pcode: '' });
    updateSchema({
      field: 'pid',
      componentProps: { treeData },
    });
  });
  //Đặt tiêu đề
  const getTitle = computed(() => (!unref(isUpdate) ? 'Thêm mới từ điển' : 'Chỉnh sửa từ điển'));

  /**
   * Lấy các nút đã mở rộng theo pid
   * @param pid
   * @param arr
   */
  function getExpandKeysByPid(pid, arr) {
    if (pid && arr && arr.length > 0) {
      for (let i = 0; i < arr.length; i++) {
        if (arr[i].key == pid && unref(expandedRowKeys).indexOf(pid) < 0) {
          //Cần lấy cùng cấpkey
          getSameLevelExpandKeysByPid(arr[i]);
          expandedRowKeys.value.push(arr[i].key);
          getExpandKeysByPid(arr[i]['parentId'], unref(treeData));
        } else {
          getExpandKeysByPid(pid, arr[i].children);
        }
      }
    }
  }
  //Sự kiện gửi biểu mẫu
  async function handleSubmit() {
    try {
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //Gửi biểu mẫu
      await saveOrUpdateDict(values, isUpdate.value);
      //Đóng cửa sổ bật lên
      closeModal();
      //Thông tin các nút đã mở rộng
      await getExpandKeysByPid(values['pid'], unref(treeData));
      //Làm mới danh sách(isUpdate:Có chỉnh sửa hay không; values: thông tin biểu mẫu; expandedArr: thông tin các nút đã mở rộng)
      emit('success', { isUpdate: unref(isUpdate), isSubAdd:unref(isSubAdd), values: { ...values }, expandedArr: unref(expandedRowKeys).reverse() });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }

  /**
   * Lấy ID cùng cấp và các mục con cùng cấpid
   */
  function getSameLevelExpandKeysByPid(arr) {
    if (arr.children && arr.children.length > 0) {
      for (const children of arr.children) {
        if (unref(expandedRowKeys).indexOf(children.key) < 0 && children.children && children.children.length > 0) {
          getSameLevelExpandKeysByPid(children);
          expandedRowKeys.value.push(children.key);
        }
      }
    }
  }
</script>
