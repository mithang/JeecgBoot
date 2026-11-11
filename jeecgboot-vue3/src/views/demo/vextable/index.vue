<template>
  <div class="p-4">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-dropdown>
          <template #overlay>
            <a-menu @click="handleCreate">
              <a-menu-item :key="1">Ví dụ một đối một</a-menu-item>
              <a-menu-item :key="2">Ví dụ một đối nhiều</a-menu-item>
              <a-menu-item :key="3">Một-nhiều(JVexTable)</a-menu-item>
            </a-menu>
          </template>
          <a-button type="primary">Thêm mới <DownOutlined /></a-button>
        </a-dropdown>
      </template>
      <template #ctype="{ text }">
        {{ text === '1' ? 'Đơn hàng nội địa' : text === '2' ? 'Đơn hàng quốc tế' : text }}
      </template>
      <template #action="{ record }">
        <TableAction :actions="getAction(record)" :dropDownActions="getDropDownActions(record)" />
      </template>
    </BasicTable>
    <!--        <TableDrawer @register="registerDrawer" @success="handleSuccess" />-->
    <TableModal @register="registerModal" @success="handleSuccess" />
    <JVxeTableModal @register="registerVexTableModal" @success="handleSuccess"></JVxeTableModal>
    <OneToOneModal @register="registerOneToOneModal" @success="handleSuccess"></OneToOneModal>
  </div>
</template>
<script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import TableDrawer from './drawer.vue';
  import TableModal from './modal.vue';
  import VexTableModal from './VexTableModal.vue';
  import JVxeTableModal from './jvxetable/JVxeTableModal.vue';
  import OneToOneModal from './OneToOneModal.vue';
  import { DownOutlined } from '@ant-design/icons-vue';
  import { useListPage } from '/@/hooks/system/useListPage';

  import { useModal } from '/@/components/Modal';
  import { columns } from './data';
  import { list, deleteOne } from './api';
  import { defHttp } from '/@/utils/http/axios';

  const [registerModal, { openModal }] = useModal();
  const [registerOneToOneModal, { openModal: openOneToOneModal }] = useModal();
  const [registerVexTableModal, { openModal: openVexTableModal }] = useModal();

  //Định nghĩa thao tác hàng bảng
  const getAction = (record) => {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
    ];
  };

  const getDropDownActions = (record) => {
    let arr = [
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Có xóa không?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
    return arr;
  };

  // Tham số, phương thức chung của trang danh sách
  const { tableContext } = useListPage({
    tableProps: {
      api: list,
      columns: columns,
      useSearchForm: false,
      actionColumn: {
        width: 160,
        title: 'Thao tác',
        dataIndex: 'action',
        slots: { customRender: 'action' },
      },
    },
  });

  //Đăng ký dữ liệu table
  const [registerTable, { reload }, { rowSelection }] = tableContext;
  //Thêm mới loại
  // Giải thích logic mã: [VUEN-1661]Ví dụ một-nhiều, khi chỉnh sửa, có lúc là một-một, có lúc là một-nhiều, mặc định là một-nhiều------------
  const addType = ref(3);
  //Thêm sự kiện
  function handleCreate(e) {
    addType.value = e.key;
    let type = addType.value;
    if (type == 1) {
      openOneToOneModal(true, {
        isUpdate: false,
      });
    }
    if (type == 2) {
      openModal(true, {
        isUpdate: false,
      });
    }
    if (type == 3) {
      openVexTableModal(true, {
        isUpdate: false,
      });
    }
  }

  //Sự kiện chỉnh sửa
  function handleEdit(record: Recordable) {
    let type = addType.value;
    if (type == 1) {
      openOneToOneModal(true, {
        record,
        isUpdate: true,
      });
    }
    if (type == 2) {
      openModal(true, {
        record,
        isUpdate: true,
      });
    }
    if (type == 3) {
      openVexTableModal(true, {
        record,
        isUpdate: true,
      });
    }
  }

  async function handleDelete(record: Recordable) {
    await deleteOne({ id: record.id }, reload);
  }

  function handleSuccess() {
    reload();
  }
</script>
