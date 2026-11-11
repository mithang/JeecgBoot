<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="800px">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button v-if="selectedRowKeys.length>0" preIcon="ant-design:delete-outlined" type="primary" @click="handleLeaveBatch" style="margin-right: 5px">Cho nghỉ việc hàng loạt</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { userColumns, userSearchFormSchema } from "../tenant.data";
import { getTenantUserList, leaveTenant } from "../tenant.api";
import { useListPage } from "/@/hooks/system/useListPage";
import { BasicTable, TableAction } from '/@/components/Table';

const tenantId = ref<number>(0);
// Tham số và phương thức dùng chung của trang danh sách
const { prefixCls, tableContext } = useListPage({
  designScope: 'tenant-template',
  tableProps: {
    api: getTenantUserList,
    columns: userColumns,
    immediate:false,
    canResize: false,
    formConfig: {
      schemas: userSearchFormSchema,
      // Giải thích logic mã: 【QQYUN-5698】Vấn đề về kiểu (style)------------
      labelWidth: 40,
      actionColOptions: {
        xs: 24,
        sm: 8,
        md: 8,
        lg: 8,
        xl: 8,
        xxl: 8,
      },
    },
    beforeFetch: (params) => {
      return Object.assign(params, { userTenantId: unref(tenantId) });
    },
  },
});
const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

// EmitsKhai báo
const emit = defineEmits(['register', 'success']);
//Gán giá trị cho biểu mẫu
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  tenantId.value = data.id;
  success();
});
//Đặt tiêu đề
const title = 'Thành viên';
//Sự kiện gửi biểu mẫu
async function handleSubmit(v) {
  closeModal();
}

function getActions(record) {
  return [
    {
      label: 'Loại bỏ',
      onClick: handleLeave.bind(null, record.id),
    },
  ]
}

/**
 * Thành công
 */
function success() {
  (selectedRowKeys.value = []) && reload();
}

/**
 * Xin nghỉ việc
 * @param id
 */
async function handleLeave(id) {
  await leaveTenant({userIds:id,tenantId:unref(tenantId)},success)
}

/**
 * Cho nghỉ việc hàng loạt
 */
async function  handleLeaveBatch(){
  await leaveTenant({userIds:selectedRowKeys.value.join(","),tenantId:unref(tenantId)},success)
}
</script>

<style scoped>
  /*update-begin---author:wangshuai ---date:20220705  for：Component truy vấn: input có hậu tố, ẩn đi------------*/
  :deep(.ant-input-suffix){
    display: none;
  }
  /*update-begin---author:wangshuai ---date:20220705  for：Component truy vấn: input có hậu tố, ẩn đi------------*/
</style>
