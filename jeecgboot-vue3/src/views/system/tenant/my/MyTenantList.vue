<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection" @fetch-success="onFetchSuccess">
      <template #tableTitle>
        <a-button
          preIcon="ant-design:user-add-outlined"
          type="primary"
          @click="handleInvitation"
          style="margin-right: 5px"
          :disabled="selectedRowKeys.length === 0"
          >Mời người dùng tham gia</a-button
        >
<!--        <a-button
          preIcon="ant-design:plus-outlined"
          type="primary"
          @click="handlePack"
          style="margin-right: 5px"
          :disabled="selectedRowKeys.length === 0"
          >Gói dịch vụ</a-button
        >-->
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" />
      </template>
    </BasicTable>
    <TenantInviteUserModal @register="registerSelUserModal" @inviteOk="handleInviteUserOk" />
    <TenantUserModal @register="registerTenUserModal" />
    <!--  Gói sản phẩm  -->
    <TenantPackList @register="registerPackModal" />
  </div>
</template>
<script lang="ts" name="tenant-my-tenant-list" setup>
  import { onMounted, ref, unref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { invitationUserJoin, getTenantPageListByUserId } from '../tenant.api';
  import { columns, searchFormSchema } from '../tenant.data';
  import TenantModal from '../components/TenantModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useListPage } from '/@/hooks/system/useListPage';
  import TenantInviteUserModal from '../components/TenantInviteUserModal.vue';
  import TenantUserModal from '../components/TenantUserList.vue';
  import TenantPackList from '../pack/TenantPackList.vue';
  import { getTenantId } from '/@/utils/auth';
  import { useUserStore } from '/@/store/modules/user';
  import { tenantSaasMessage } from "@/utils/common/compUtils";

  const { createMessage } = useMessage();
  const [registerModal, { openModal }] = useModal();
  const [registerSelUserModal, { openModal: userOpenModal }] = useModal();
  const [registerTenUserModal, { openModal: tenUserOpenModal }] = useModal();
  const [registerPackModal, { openModal: packModal }] = useModal();
  const userStore = useUserStore();

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    designScope: 'tenant-template',
    tableProps: {
      title: 'Danh sách đơn vị thuê',
      api: getTenantPageListByUserId,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      actionColumn: {
        width: 150,
        fixed: 'right',
      },
      rowSelection:{
        type: "radio",
      },
      beforeFetch: (params) => {
        return Object.assign(params, { userTenantStatus: '1,3,4' });
      },
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  // Mặc định chọn đơn vị thuê hiện tại
  function onFetchSuccess(data) {
    let items = data.items;
    console.log('items:', items);
    // Đơn vị thuê đang đăng nhậpID
    let loginTenantId = getTenantId();
    console.log('loginTenantId:', loginTenantId);
    // Nếu ID đơn vị thuê đang đăng nhập có trong danh sách thì chọn mặc định
    if (items && items.length > 0 && loginTenantId) {
      for (let i = 0; i < items.length; i++) {
        if (items[i].id == loginTenantId) {
          console.log('items[i].id:', items[i].id);
          selectedRowKeys.value = [items[i].id];
          selectedRows.value = [items[i]];
          return;
        }
      }
    }
  }

  /**
   * Định nghĩa cột thao tác
   * @param record
   */
  function getActions(record) {
    return [
      {
        label: 'Người dùng',
        onClick: handleSeeUser.bind(null, record.id),
      },
    ];
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
  async function handleSeeUser(id) {
    tenUserOpenModal(true, {
      id: id,
    });
  }

  /**
   * Thêm mới gói sản phẩm
   */
  async function handlePack() {
    if (unref(selectedRowKeys).length > 1) {
      createMessage.warn('Vui lòng chọn một mục');
      return;
    }
    packModal(true, {
      tenantId: unref(selectedRowKeys.value.join(',')),
      //Đơn vị thuê của tôi không hiển thị thêm mới và chỉnh sửa gói sản phẩm
      showPackAddAndEdit: false
    });
  }

  /**
   * Sự kiện callback sau khi xóa thành công
   */
  function handleSuccess() {
    (selectedRowKeys.value = []) && reload();
  }

  onMounted(()=>{
    //Thông tin thông báo
    tenantSaasMessage('Đơn vị thuê của tôi')
  })
</script>
