<template>
  <div>
    <!--Tham chiếu bảng-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--Khe cắm:table tiêu đề-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate">Thêm mới</a-button>
        <a-button
            preIcon="ant-design:user-add-outlined"
            type="primary"
            @click="handleInvitation"
            style="margin-right: 5px">
            Mời người dùng tham gia
        </a-button>
        <JThirdAppButton biz-type="user" :selected-row-keys="selectedRowKeys" syncToApp syncToLocal @sync-finally="onSyncFinally" />
        <a-button type="primary" @click="openQuitModal(true, {})" preIcon="ant-design:user-delete-outlined">Nhân viên nghỉ việc</a-button>
        <div style="margin-left: 10px;margin-top: 5px"> Đơn vị thuê đang đăng nhập: <span class="tenant-name">{{loginTenantName}}</span> </div>
        <a-tooltip title="Giải thích thao tác khác của người dùng đơn vị thuê">
          <a-icon type="question-circle" style="margin-left: 8px; cursor: pointer " @click="tipShow = true"/>
        </a-tooltip>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!--Ngăn kéo người dùng-->
    <TenantUserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!-- Hộp thoại danh sách nhân viên nghỉ việc -->
    <UserQuitModal @register="registerQuitModal" @success="reload" />
    <!--  Hộp thoại thay đổi chủ sở hữu  -->
    <UserSelectModal @register="registerUserModal" :excludeUserIdList="excludeUserIdList" :maxSelectCount="1" @getSelectResult="selectResult" />
    <!--  Hộp thoại phân bổ gói dịch vụ  -->
    <TenantPackAllotModal @register="registerPackAllotModal"></TenantPackAllotModal>
    <!--  Hộp thoại người mời  -->
    <TenantInviteUserModal @register="registerSelUserModal" @inviteOk="handleInviteUserOk" />
    <a-modal v-model:open="tipShow" :footer="null" title="Giải thích thao tác khác của người dùng đơn vị thuê" :width="800">
      <ul class="user-tenant-tip">
        <li>Loại bỏ: loại bỏ người dùng khỏi đơn vị thuê hiện tại</li>
        <li>Xóa: chỉ có thể xóa người dùng được tạo trong ngày, sau khi xóa có thể khôi phục trong thùng rác người dùng hệ thống</li>
        <li>Nghỉ việc: người không phải người tạo đơn vị thuê có thể thực hiện thao tác nghỉ việc, nhân viên nghỉ việc có thể xem trong danh sách nhân viên nghỉ việc</li>
        <li>Bàn giao: người tạo đơn vị thuê có thể bàn giao đơn vị thuê, sau khi bàn giao có thể xem thông tin nhân viên trong danh sách nhân viên nghỉ việc</li>
      </ul>
      <div style="height: 10px"></div>
    </a-modal>
  </div>
</template>

<script lang="ts" name="tenant-system-user" setup>
  //tsCú pháp
  import { onMounted, ref, unref } from 'vue';
  import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
  import UserDrawer from '../user/UserDrawer.vue';
  import JThirdAppButton from '/@/components/jeecg/thirdApp/JThirdAppButton.vue';
  import UserQuitModal from '../user/UserQuitModal.vue';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useModal } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { columns, searchFormSchema } from '../user/user.data';
  import { list , deleteUser, batchDeleteUser, getImportUrl, getExportUrl, frozenBatch, getUserTenantPageList, updateUserTenantStatus } from '../user/user.api';
  // import { usePermission } from '/@/hooks/web/usePermission'
  // const { hasPermission } = usePermission();
  import { userTenantColumns, userTenantFormSchema } from '../user/user.data';
  import { useUserStore } from '/@/store/modules/user';
  import UserSelectModal from '/@/components/Form/src/jeecg/components/modal/UserSelectModal.vue';
  import { getTenantId } from "/@/utils/auth";
  import { changeOwenUserTenant } from "/@/views/system/usersetting/UserSetting.api";
  import {getLoginTenantName, invitationUserJoin, leaveTenant} from "/@/views/system/tenant/tenant.api";
  import TenantUserDrawer from './components/TenantUserDrawer.vue';
  import { sameDay, tenantSaasMessage } from "@/utils/common/compUtils";
  import TenantPackAllotModal from './components/TenantPackAllotModal.vue'
  import TenantInviteUserModal from "@/views/system/tenant/components/TenantInviteUserModal.vue";

  const { createMessage, createConfirm } = useMessage();

  //Đăng kýdrawer
  const [registerDrawer, { openDrawer }] = useDrawer();
  //Người được ủy quyền khi nghỉ việcmodel
  const [registerQuitAgentModal, { openModal: openQuitAgentModal }] = useModal();
  //Danh sách người dùng nghỉ việcmodel
  const [registerQuitModal, { openModal: openQuitModal }] = useModal();
  //Hộp thoại phân bổ gói dịch vụ
  const [registerPackAllotModal, { openModal: openPackAllotModal }] = useModal();
  const userStore = useUserStore();
  const createBy = userStore.getUserInfo.username;
  //Hiển thị nhắc hộp thoại
  const tipShow = ref<boolean>(false);

  // Tham số và phương thức dùng chung của trang danh sách
  const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
    designScope: 'user-list',
    tableProps: {
      title: 'Danh sách người dùng đơn vị thuê',
      api: getUserTenantPageList,
      columns: userTenantColumns,
      size: 'small',
      formConfig: {
        schemas: userTenantFormSchema,
      },
      actionColumn: {
        width: 120,
      },
      beforeFetch: (params) => {
        params['userTenantStatus'] = '1,3,4';
        return Object.assign({ column: 'createTime', order: 'desc' }, params);
      },
    },
  });

  //Đăng ký dữ liệu table
  const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

  /**
   * Sự kiện thêm mới
   */
  function handleCreate() {
    openDrawer(true, {
      isUpdate: false,
      showFooter: true,
      tenantSaas: true,
    });
  }
  /**
   * Sự kiện chỉnh sửa
   */
  async function handleEdit(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: true,
      tenantSaas: true,
    });
  }
  /**
   * Chi tiết
   */
  async function handleDetail(record: Recordable) {
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
      tenantSaas: true,
    });
  }

  /**
   * Callback thành công
   */
  function handleSuccess() {
    reload();
  }

  /**
   *Đồng bộ callback của DingTalk và WeChat
   */
  function onSyncFinally({ isToLocal }) {
    // Khi đồng bộ về máy cục bộ thì làm mới dữ liệu
    if (isToLocal) {
      reload();
    }
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
        // ifShow: () => hasPermission('system:user:edit'),
      },
    ];
  }
  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      {
        label: 'Xem chi tiết',
        onClick: handleDetail.bind(null, record),
      },
      {
        label: 'Loại bỏ người dùng',
        onClick: handleLeave.bind(null, record.id),
      },
      {
        label: 'Xóa người dùng',
        popConfirm: {
          title: 'Xác nhận xóa người dùng này?',
          confirm: handleDeleteUser.bind(null, record),
        },
        ifShow: () => record.username!== userStore.getUserInfo?.username && sameDay(record.createTime),
      },
      {
        label: 'Thay đổi chủ sở hữu',
        onClick: handleHandover.bind(null, record),
        ifShow: () =>{
          return record.username === record.createBy;
        }
      },
      {
        label: 'Đồng ý',
        onClick: updateStatus.bind(null, record.id, '1'),
        ifShow: () => {
          return (record.status === '3' || record.status === '4') && record.createBy === createBy;
        },
      },
      {
        label: 'Từ chối',
        popConfirm: {
          title: 'Xác nhận từ chối?',
          confirm: updateStatus.bind(null, record.id, '4'),
        },
        ifShow: () => {
          return record.status === '3' && record.createBy === createBy;
        },
      },
      {
        label: 'Gói của người dùng',
        onClick: handleAllotPack.bind(null, record),
      }
    ];
  }

  /**
   * Cập nhật trạng thái đơn vị thuê của người dùng
   * @param id
   * @param status
   */
  function updateStatus(id, status) {
    updateUserTenantStatus({ userId: id, status: status })
      .then((res) => {
        if (res.success) {
          handleSuccess();
        } else {
          createMessage.warning(res.message);
        }
      })
      .catch((e) => {
        createMessage.warning(e.message);
      });
  }

  //============================================ Bàn giao khi nghỉ việc của đơn vị thuê  ============================================
  //Đơn vị thuê (tenant)id
  const tenantId = ref<string>('');
  //Tập hợp mã số loại trừ chính mình
  const excludeUserIdList = ref<any>([]);
  //Người được ủy quyền khi nghỉ việcmodel
  const [registerUserModal, { openModal: openUserModal }] = useModal();
  //Hộp thoại mời người dùng tham gia
  const [registerSelUserModal, { openModal: userOpenModal }] = useModal();
  const handOverUserName = ref<string>('');
  
  /**
   * Bàn giao nhân sự
   */
  function handleHandover(record) {
    tenantId.value = getTenantId();
    excludeUserIdList.value = [record.id];
    //Ghi lại tên người dùng cần bàn giao hiện tại
    handOverUserName.value = record.createBy;
    openUserModal(true)
  }

  /**
   * Callback chọn người dùng
   * @param options
   * @param values
   */
  function selectResult(options,values) {
    console.log(values)
    if(values && values.length>0){
      let userId = values[0];
      changeOwenUserTenant({ userId:userId, tenantId:unref(tenantId) }).then((res) =>{
        if(res.success){
          createMessage.success("Bàn giao thành công");
          let username = userStore.getUserInfo?.username;
          if(username == handOverUserName.value){
            userStore.logout(true);
          }else{
            reload();
          }
        } else {
          createMessage.warning(res.message);
        }
      })
    }
  }
  //============================================  Bàn giao khi nghỉ việc của đơn vị thuê  ============================================


  // Giải thích logic mã: 【QQYUN-5723】4、Hiển thị đơn vị thuê đang đăng nhập------------
  const loginTenantName = ref<string>('');

  getTenantName();

  async function getTenantName(){
    loginTenantName.value = await getLoginTenantName();
  }
  //update-end---author:wangshuai ---date:20230710  for：【QQYUN-5723】4、Hiển thị đơn vị thuê đang đăng nhập------------
  

  /**
   * Phân bổ gói dịch vụ
   * 
   * @param record
   */
  function handleAllotPack(record) {
    openPackAllotModal(true,{
      record
    })
  }

  /**
   * Xóa người dùng
   */
  function handleDeleteUser(record) {
    deleteUser({ id: record.id }, reload);
  }

  /**
   * Mời người dùng tham gia đơn vị thuê
   */
  function handleInvitation() {
    userOpenModal(true, {});
  }
  
  /**
   * Sự kiện callback chọn người dùng
   * @param username
   * @param phone
   * @param userSelectId
   */
  async function handleInviteUserOk(phone, username) {
    let tId = getTenantId();
    if (phone) {
      await invitationUserJoin({ ids: tId, phone: phone });
      reload();
    }
    if (username) {
      await invitationUserJoin({ ids: tId, username: username });
      reload();
    }
  }

  /**
   * Xin nghỉ việc
   * @param id
   */
  async function handleLeave(id) {
    await leaveTenant({ userIds: id, tenantId: getTenantId() }, reload)
  }
  
  
  onMounted(()=>{
    tenantSaasMessage('Người dùng đơn vị thuê')
  })
</script>

<style scoped>
  .tenant-name{
    text-decoration:underline;
    margin: 5px;
    font-size: 15px;
  }
  .user-tenant-tip{
    margin: 20px;
    background-color: #f8f9fb;
    color: #99a1a9;
    border-radius: 4px;
    padding: 12px;
  }
</style>
