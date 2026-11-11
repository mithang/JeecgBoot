<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" @ok="handleSubmit" width="800px" :showCancelBtn="false" :showOkBtn="false">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:plus-outlined" type="primary" @click="handleAdd" style="margin-right: 5px" v-if="showPackAddAndEdit">Thêm mới </a-button>
        <a-button
          v-if="selectedRowKeys.length > 0"
          preIcon="ant-design:delete-outlined"
          type="primary"
          @click="handlePackBatch"
          style="margin-right: 5px"
          >Xóa hàng loạt
        </a-button>
        <a-button
          preIcon="ant-design:sync-outlined"
          type="primary"
          @click="handleSyncDefaultPack"
          style="margin-right: 5px"
          >Khởi tạo gói mặc định
        </a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getActions(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
  <TenantPackMenuModal @register="registerPackMenu" @success="success" />
  <TenantPackUserModal @register="registerPackUser" @success="success" />
  <PackPermissionDrawer @register="registerPackPermDrawer" @success="success"/>
</template>
<script lang="ts" setup name="tenant-pack-modal">
  import { reactive, ref, unref } from 'vue';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { packColumns, userColumns, packFormSchema } from '../tenant.data';
  import { getTenantUserList, leaveTenant, packList, deleteTenantPack, syncDefaultTenantPack } from '../tenant.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { BasicTable, TableAction } from '/@/components/Table';
  import TenantPackMenuModal from './TenantPackMenuModal.vue';
  import {Modal} from "ant-design-vue";
  import TenantPackUserModal from './TenantPackUserModal.vue';
  import {useMessage} from "/@/hooks/web/useMessage";
  import PackPermissionDrawer from "@/views/system/tenant/pack/PackPermissionDrawer.vue";
  import { useDrawer } from "@/components/Drawer";

  const [registerPackMenu, { openModal }] = useModal();
  const [registerPackUser, { openModal: packUserOpenModal }] = useModal();
  const [registerPackPermDrawer, { openDrawer: openPackPermDrawer }] = useDrawer();
  
  const tenantId = ref<number>(0);
  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext } = useListPage({
    designScope: 'tenant-template',
    tableProps: {
      api: packList,
      columns: packColumns,
      immediate: false,
      formConfig: {
        schemas: packFormSchema,
        labelCol: {
          xxl: 8,
        },
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
        return Object.assign(params, { tenantId: unref(tenantId), packType:'custom' });
      },
    },
  });
  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
  // EmitsKhai báo
  const emit = defineEmits(['register', 'success']);
  //Có hiển thị thêm mới và chỉnh sửa gói dịch vụ không
  const showPackAddAndEdit = ref<boolean>(false);
  //Gán giá trị cho biểu mẫu
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    tenantId.value = data.tenantId;
    showPackAddAndEdit.value = data.showPackAddAndEdit;
    success();
  });
  //Đặt tiêu đề
  const title = 'Gói dịch vụ cá nhân hóa của đơn vị thuê';

  //Sự kiện gửi biểu mẫu
  async function handleSubmit(v) {
    closeModal();
  }

  function getActions(record) {
    return [
      {
        label: 'Người dùng',
        onClick: seeTenantPackUser.bind(null, record),
      },
      {
        label: 'Cấp quyền',
        onClick: handleRolePrem.bind(null, record),
        ifShow: ()=>{ return showPackAddAndEdit.value }
      },
    ];
  }

  /**
   * Thành công
   */
  function success() {
    (selectedRowKeys.value = []) && reload();
  }

  /**
   * Chỉnh sửa
   * @param record
   */
  async function handleEdit(record) {
    openModal(true, {
      isUpdate: true,
      record: record,
      tenantId: unref(tenantId),
      packType:'custom',
      showFooter: true
    });
  }

  //Gói dịch vụ mặc định của hệ thống không được phép xóa,Bao gồm(Siêu quản trị viên, quản trị viên tài khoản tổ chức, quản trị viên ứng dụng tổ chức)
  const packCode = reactive<any>(['superAdmin','accountAdmin','appAdmin']);
  const { createMessage } = useMessage();
  
  /**
   * Xóa gói dịch vụ
   * @param Xóa
   */
  async function handleDelete(record) {
    // Giải thích logic mã: Gói dịch vụ mặc định của hệ thống không được phép xóa------------
    if(packCode.indexOf(record.packCode) != -1){
        createMessage.warning("Gói dịch vụ mặc định của hệ thống không được phép xóa");
       return;
    }
    if(record.packCode && record.packCode.indexOf("default") != -1){
      createMessage.warning("Gói dịch vụ mặc định không được phép xóa");
      return;
    }
    await deleteTenantPack({ ids: record.id }, success);
  }

  /**
   * Xóa hàng loạt gói dịch vụ
   */
  async function handlePackBatch() {
    let value = selectedRows.value;
    if(value && value.length>0){
      for (let i = 0; i < value.length; i++) {
        if(packCode.indexOf(value[i].packCode) != -1){
          createMessage.warning("Gói dịch vụ mặc định của hệ thống không được phép xóa");
          return;
        }
        // Giải thích logic mã: Gói mặc định không được phép xóa---
        if(value[i].packCode && value[i].packCode.indexOf("default") != -1){
          createMessage.warning("Gói dịch vụ mặc định không được phép xóa");
          return;
        }
      }
    }
    Modal.confirm({
      title: 'Xóa gói dịch vụ của đơn vị thuê',
      content: 'Xóa gói dịch vụ của đơn vị thuê?',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: async () => {
        await deleteTenantPack({ ids: selectedRowKeys.value.join(',')}, success);
      }
    })
  }

  async function handleSyncDefaultPack() {
    Modal.confirm({
      title: 'Khởi tạo gói dịch vụ mặc định',
      content: 'Có khởi tạo gói dịch vụ mặc định hay không',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: async () => {
        await syncDefaultTenantPack({tenantId: unref(tenantId)}, success);
      },
    });
  }

  /**
   *
   * Thêm mới biểu mẫu
   */
  function handleAdd() {
    openModal(true, {
      isUpdate: false,
      tenantId: unref(tenantId),
      packType:'custom',
      showFooter: true
    });
  }

  /**
   * Người dùng thuộc gói dịch vụ
   * @param record
   */
  function seeTenantPackUser(record) {
    packUserOpenModal(true,{
      record:record
    })
  }

  /**
   * Thêm
   * @param record
   */
  function getDropDownAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
        ifShow: ()=>{ return showPackAddAndEdit.value }
      },
      {
        label: 'Chi tiết',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa gói dịch vụ của đơn vị thuê?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ]
  }

  /**
   * Chi tiết
   * @param record
   */
  function handleDetail(record) {
    openModal(true, {
      isUpdate: true,
      record: record,
      tenantId: unref(tenantId),
      packType:'custom',
      showFooter: false
    });
  }


  /**
   * Cấp quyền
   *
   * @param record
   */
  function handleRolePrem(record) {
    openPackPermDrawer(true,{
      packId: record.id,
      permissionIds: record.permissionIds
    })
  }
</script>
