<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Danh sách chọn người dùng" width="1000px" @ok="handleSubmit" destroyOnClose @openChange="handleOpenChange">
    <BasicTable @register="registerTable" :rowSelection="rowSelection" />
  </BasicModal>
</template>
<script lang="ts" setup>
  import { ref, unref, toRaw } from 'vue';
  import { BasicModal, useModalInner } from '/src/components/Modal';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { userColumns, searchUserFormSchema } from '../role.data';
  import { list } from '../../user/user.api';
  // Khai báoEmits
  const emit = defineEmits(['select', 'register']);
  const checkedKeys = ref<Array<string | number>>([]);
  const [registerModal, { setModalProps, closeModal }] = useModalInner();
  //Đăng ký dữ liệu table
  const [registerTable, { reload }] = useTable({
    api: list,
    rowKey: 'id',
    columns: userColumns,
    formConfig: {
      labelWidth: 60,
      schemas: searchUserFormSchema,
      baseRowStyle: { maxHeight: '20px' },
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    showTableSetting: false,
    bordered: true,
    showIndexColumn: false,
    canResize: false,
  });
  /**
   * Cấu hình cột chọn
   */
  const rowSelection = {
    type: 'checkbox',
    columnWidth: 50,
    selectedRowKeys: checkedKeys,
    onChange: onSelectChange,
  };
  /**
   * Sự kiện chọn
   */
  function onSelectChange(selectedRowKeys: (string | number)[]) {
    checkedKeys.value = selectedRowKeys;
  }

  const handleOpenChange = (visible) => {
    // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1679】Vai trò hệ thống - Người dùng của vai trò: khi mở lại hộp thoại thì đặt lại trạng thái đã chọn trước đó
    if (visible) {
      checkedKeys.value = [];
    }
    // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1679】Vai trò hệ thống - Người dùng của vai trò: khi mở lại hộp thoại thì đặt lại trạng thái đã chọn trước đó
  };

  //Sự kiện gửi
  function handleSubmit() {
    setModalProps({ confirmLoading: true });
    //Đóng cửa sổ bật lên
    closeModal();
    //Làm mới danh sách
    emit('select', toRaw(unref(checkedKeys)));
    setModalProps({ confirmLoading: false });
  }
</script>
