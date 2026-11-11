<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd" style="margin-right: 5px">Thêm mới</a-button>
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
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
        <a-button
          preIcon="ant-design:user-add-outlined"
          type="primary"
          @click="handleInvitation"
          style="margin-right: 5px"
          :disabled="selectedRowKeys.length === 0"
          >Mời người dùng tham gia</a-button
        >
        <a-button
          preIcon="ant-design:sliders-outlined"
          type="primary"
          @click="handlePack"
          style="margin-right: 5px"
          :disabled="selectedRowKeys.length === 0"
          >Quản lý gói dịch vụ</a-button
        >
        <a-button type="primary" @click="recycleBinClick" preIcon="ant-design:hdd-outlined">Thùng rác</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <TenantModal @register="registerModal" @success="reload" />
    <TenantInviteUserModal @register="registerSelUserModal" @inviteOk="handleInviteUserOk"/>
    <TenantUserModal @register="registerTenUserModal" />
    <!--  Gói sản phẩm  -->
    <TenantPackList @register="registerPackModal" />
    <!--  Thùng rác đơn vị thuê  -->
    <TenantRecycleBinModal @register="registerRecycleBinModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="system-tenant" setup>
  import { ref, unref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { getTenantList, deleteTenant, batchDeleteTenant, invitationUserJoin } from './tenant.api';
  import { columns, searchFormSchema } from './tenant.data';
  import TenantModal from './components/TenantModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import TenantInviteUserModal from './components/TenantInviteUserModal.vue';
  import TenantUserModal from './components/TenantUserList.vue';
  import TenantPackList from './pack/TenantPackList.vue';
  import TenantRecycleBinModal from './components/TenantRecycleBinModal.vue';

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerSelUserModal, { openModal: userOpenModal }] = useModal();
  const [registerTenUserModal, { openModal: tenUserOpenModal }] = useModal();
  const [registerPackModal, { openModal: packModal }] = useModal();
  const [registerRecycleBinModal, { openModal: recycleBinModal }] = useModal();

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    designScope: 'tenant-template',
    tableProps: {
      title: 'Danh sách đơn vị thuê',
      api: getTenantList,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
        fieldMapToTime: [['fieldTime', ['beginDate', 'endDate'], 'YYYY-MM-DD HH:mm:ss']],
      },
      actionColumn:{
        width: 150,
        fixed:'right'
      }
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa?',
          placement: 'left',
          confirm: handleDelete.bind(null, record),
        },
      },
      {
        label: 'Người dùng',
        onClick: handleSeeUser.bind(null, record.id),
      },
    ];
  }

  /**
   * Sự kiện thêm mới
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * Sự kiện chỉnh sửa
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Sự kiện xóa
   */
  async function handleDelete(record) {
    await deleteTenant({ id: record.id }, handleSuccess);
  }

  /**
   * Sự kiện xóa hàng loạt
   */
  async function batchHandleDelete() {
    await batchDeleteTenant({ ids: selectedRowKeys.value }, handleSuccess);
  }

  /**
   * Mời người dùng tham gia đơn vị thuê
   */
  function handleInvitation() {
    userOpenModal(true, {});
  }

  /**
   * Sự kiện callback chọn người dùng
   * @param options
   * @param phone
   * @param userSelectId
   */
  async function handleInviteUserOk(phone, username) {
    // Giải thích logic mã: 【QQYUN-4605】Phần mời người tham gia đơn vị thuê ở hậu trường không có cách nào chọn người dùng không thuộc đơn vị thuê------------
    if (phone) {
      await invitationUserJoin({ ids: selectedRowKeys.value.join(','), phone: phone });
    }
    if (username) {
      await invitationUserJoin({ ids: selectedRowKeys.value.join(','), username: username });
    }
  }

  /**
   * Xem người dùng
   * @param id
   */
  function handleSeeUser(id) {
    tenUserOpenModal(true, {
      id: id,
    });
  }

  /**
   * Thêm mới gói sản phẩm
   */
  function handlePack() {
    if (unref(selectedRowKeys).length > 1) {
      createMessage.warn('Vui lòng chọn một mục');
      return;
    }
    packModal(true, {
      tenantId: unref(selectedRowKeys.value.join(',')),
      //Đơn vị thuê của tôi hiển thị thêm mới và chỉnh sửa gói sản phẩm
      showPackAddAndEdit: true
    });
  }

  /**
   * Thùng rác
   */
  function recycleBinClick() {
    recycleBinModal(true, {});
  }

  /**
   * Sự kiện callback sau khi xóa thành công
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }
</script>
