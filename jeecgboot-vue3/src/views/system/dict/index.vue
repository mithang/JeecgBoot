<template>
  <!--Tham chiếu bảng-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--Khe cắm:table tiêu đề-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> Thêm mới</a-button>
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> Xuất (export)</a-button>
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">Nhập (import)</j-upload-button>
      <a-button type="primary" @click="handlerRefreshCache" preIcon="ant-design:sync-outlined"> Làm mới bộ nhớ đệm</a-button>
      <a-button type="primary" @click="openRecycleModal(true)" preIcon="ant-design:hdd-outlined"> Thùng rác</a-button>

      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchHandleDelete">
              <Icon icon="ant-design:delete-outlined"></Icon>
              Xóa
            </a-menu-item>
          </a-menu>
        </template>
        <a-button
          >Thao tác hàng loạt
          <Icon icon="ant-design:down-outlined"></Icon>
        </a-button>
      </a-dropdown>
    </template>
    <!--Thanh thao tác-->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  <!--Hộp thoại từ điển-->
  <DictModal @register="registerModal" @success="handleSuccess" />
  <!--Ngăn kéo cấu hình từ điển-->
  <DictItemList @register="registerDrawer" />
  <!--Hộp thoại thùng rác-->
  <DictRecycleBinModal @register="registerModal1" @success="reload" />
</template>

<script lang="ts" name="system-dict" setup>
  //tsCú pháp
  import { ref, computed, unref } from 'vue';
  import { BasicTable, TableAction } from '/src/components/Table';
  import { useDrawer } from '/src/components/Drawer';
  import { useModal } from '/src/components/Modal';
  import DictItemList from './components/DictItemList.vue';
  import DictModal from './components/DictModal.vue';
  import DictRecycleBinModal from './components/DictRecycleBinModal.vue';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { removeAuthCache, setAuthCache } from '/src/utils/auth';
  import { columns, searchFormSchema } from './dict.data';
  import { list, deleteDict, batchDeleteDict, getExportUrl, getImportUrl, refreshCache, queryAllDictItems } from './dict.api';
  import { DB_DICT_DATA_KEY } from '/src/enums/cacheEnum';
  import { useUserStore } from '/@/store/modules/user';

  const { createMessage } = useMessage();
  //Từ điểnmodel
  const [registerModal, { openModal }] = useModal();
  //Cấu hình từ điểndrawer
  const [registerDrawer, { openDrawer }] = useDrawer();
  import { useListPage } from '/@/hooks/system/useListPage';

  //Thùng rácmodel
  const [registerModal1, { openModal: openRecycleModal }] = useModal();

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'dict-template',
    tableProps: {
      title: 'Từ điển dữ liệu',
      api: list,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 240,
      },
    },
    // Giải thích logic mã: [issues/I5AMDD]Chức năng nhập/xuất, sau khi thao tác nhắc rằng chưa truyền tham số export.url/import.url------------
    exportConfig: {
      name: 'Danh sách từ điển dữ liệu',
      url: getExportUrl,
    },
    importConfig: {
      url: getImportUrl,
    },
  });

  //Đăng ký dữ liệu table
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  /**
   * Sự kiện thêm mới
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }
  /**
   * Sự kiện chỉnh sửa
   */
  async function handleEdit(record: Recordable) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Chi tiết
   */
  async function handleDetail(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }
  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteDict({ id: record.id }, () => {
      reload();
      handlerRefreshCache(false);
    });
  }
  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteDict({ ids: selectedRowKeys.value }, () => {
      // Giải thích logic mã: 【TV360X-1665】Sau khi xóa hàng loạt từ điển dữ liệu, danh sách đã chọn cũng bị xóa
      reload();
      selectedRowKeys.value = [];
      selectedRows.value = [];
      handlerRefreshCache(false);
    });
  }
  /**
   * Callback thành công
   */
  async function handleSuccess({ isUpdate, values }) {
    if (isUpdate) {
      updateTableDataRecord(values.id, values);
    } else {
      reload();
    }
    // Giải thích logic mã: Sau khi thêm mới/chỉnh sửa từ điển thì đồng bộ làm mới bộ nhớ đệm từ điển, tránh việc trang không tham chiếu được hoặc hiển thị giá trị cũ
    await handlerRefreshCache(false);
  }
  /**
   * Làm mới bộ nhớ đệm
   */
  async function handlerRefreshCache(showTip = true) {
    const result = await refreshCache();
    if (result.success) {
      const res = await queryAllDictItems();
      removeAuthCache(DB_DICT_DATA_KEY);
      // Giải thích logic mã: 【QQYUN-6417】Sự cố từ điển chậm trong môi trường production
      const userStore = useUserStore();
      userStore.setAllDictItems(res.result);
      showTip && createMessage.success('Làm mới bộ nhớ đệm hoàn thành!');
    } else {
      showTip && createMessage.error('Làm mới bộ nhớ đệm thất bại!');
    }
  }
  /**
   * Cấu hình từ điển
   */
  function handleItem(record) {
    openDrawer(true, {
      id: record.id,
    });
  }
  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Cấu hình từ điển',
        onClick: handleItem.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Bạn có chắc muốn xóa không?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
</script>

<style scoped></style>
