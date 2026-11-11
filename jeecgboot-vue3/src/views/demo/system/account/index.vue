<template>
  <PageWrapper dense contentFullHeight fixedHeight contentClass="flex">
    <DeptTree class="w-1/4 xl:w-1/5" @select="handleSelect" />
    <BasicTable @register="registerTable" class="w-3/4 xl:w-4/5" :searchInfo="searchInfo">
      <template #toolbar>
        <a-button type="primary" @click="handleCreate">Thêm mới tài khoản</a-button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:info-standard-line',
                tooltip: 'Xem chi tiết người dùng',
                onClick: handleView.bind(null, record),
              },
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Chỉnh sửa hồ sơ người dùng',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                tooltip: 'Xóa tài khoản này',
                popConfirm: {
                  title: 'Có xác nhận xóa không',
                  placement: 'left',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <AccountModal @register="registerModal" @success="handleSuccess" />
  </PageWrapper>
</template>
<script lang="ts">
  import { defineComponent, reactive } from 'vue';

  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { getAccountList } from '/@/api/demo/system';
  import { PageWrapper } from '/@/components/Page';
  import DeptTree from './DeptTree.vue';

  import { useModal } from '/@/components/Modal';
  import AccountModal from './AccountModal.vue';

  import { columns, searchFormSchema } from './account.data';
  import { useGo } from '/@/hooks/web/usePage';

  export default defineComponent({
    name: 'system-account',
    components: { BasicTable, PageWrapper, DeptTree, AccountModal, TableAction },
    setup() {
      const go = useGo();
      const [registerModal, { openModal }] = useModal();
      const searchInfo = reactive<Recordable>({});
      const [registerTable, { reload, updateTableDataRecord }] = useTable({
        title: 'Danh sách tài khoản',
        api: getAccountList,
        rowKey: 'id',
        columns,
        formConfig: {
          labelWidth: 120,
          schemas: searchFormSchema,
          autoSubmitOnEnter: true,
        },
        useSearchForm: true,
        showTableSetting: true,
        bordered: true,
        handleSearchInfoFn(info) {
          console.log('handleSearchInfoFn', info);
          return info;
        },
        actionColumn: {
          width: 120,
          title: 'Thao tác',
          dataIndex: 'action',
          // slots: { customRender: 'action' },
        },
      });

      function handleCreate() {
        openModal(true, {
          isUpdate: false,
        });
      }

      function handleEdit(record: Recordable) {
        console.log(record);
        openModal(true, {
          record,
          isUpdate: true,
        });
      }

      function handleDelete(record: Recordable) {
        console.log(record);
      }

      function handleSuccess({ isUpdate, values }) {
        if (isUpdate) {
          // Demo cập nhật dữ liệu nội bộ trực tiếp mà không làm mới bảng.
          // Lưu ý: updateTableDataRecord yêu cầu thuộc tính rowKey của bảng là string và tồn tại trong keys của record ở mỗi hàng
          const result = updateTableDataRecord(values.id, values);
          console.log(result);
        } else {
          reload();
        }
      }

      function handleSelect(deptId = '') {
        searchInfo.deptId = deptId;
        reload();
      }

      function handleView(record: Recordable) {
        go('/system/account_detail/' + record.id);
      }

      return {
        registerTable,
        registerModal,
        handleCreate,
        handleEdit,
        handleDelete,
        handleSuccess,
        handleSelect,
        handleView,
        searchInfo,
      };
    },
  });
</script>
