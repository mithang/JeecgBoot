<template>
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
</template>

<script lang="ts" name="online-user" setup>
import { BasicTable, TableAction } from '/@/components/Table';
import { columns, searchFormSchema } from './OnlineUser.data';
import { list, forceLogout } from './OnlineUser.api';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from "/@/hooks/web/useMessage";
// Tham số và phương thức dùng chung của trang danh sách
const { prefixCls, tableContext, onImportXls, onExportXls } = useListPage({
  designScope: 'online-user',
  tableProps: {
    //rowKey mặc định id của người dùng trực tuyến sẽ gây trùng key, khiến trang xuất hiện dữ liệu trùng lặp
    rowKey:'token',
    title: 'Người dùng trực tuyến',
    api: list,
    columns: columns,
    formConfig: {
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 120,
    },
    rowSelection: null,
  },
});
const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;
const $message = useMessage();

//Thanh thao tác
function getTableAction(record) {
  return [
    {
      label: 'Buộc thoát',
      popConfirm: {
        title: 'Buộc đăng xuất người dùng?',
        confirm: handleForce.bind(null, record),
      },
    },
  ];
}

/**
 * Buộc thoát
 * @param record
 */
function handleForce(record) {
   forceLogout({ token: record.token }).then((res)=>{
     if(res.success){
       reload();
       $message.createMessage.success('Buộc đăng xuất người dùng”'+record.realname+'“Thành công!');
     }else{
       $message.createMessage.warn(res.message);
     }
   })
}
</script>

<style scoped>

</style>