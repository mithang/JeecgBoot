<template>
  <!--Tham chiếu bảng-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--Khe cắm:table tiêu đề-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="selectAddUser" :disabled="!departId">Thêm người dùng hiện có</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="createUser" :disabled="!departId">Tạo mới người dùng</a-button>
      <template v-if="selectedRowKeys.length > 0">
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onUnlinkDepartUserBatch">
                <icon icon="bx:bx-unlink" />
                <span>Hủy bỏ liên kết</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>
            <span>Thao tác hàng loạt </span>
            <icon icon="akar-icons:chevron-down" />
          </a-button>
        </a-dropdown>
      </template>
    </template>
    <!-- Slot: nút thao tác trong hàng -->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <UserDrawer @register="registerDrawer" @success="onUserDrawerSuccess" />
  <DepartRoleUserAuthDrawer @register="registerUserAuthDrawer" />
  <UserSelectModal ref="userSelectModalRef" rowKey="id" @register="registerSelUserModal" @getSelectResult="onSelectUserOk" />
</template>

<script lang="ts" setup>
  import { computed, inject, ref, unref, watch } from 'vue';
  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import UserDrawer from '/@/views/system/user/UserDrawer.vue';
  import UserSelectModal from '/@/components/Form/src/jeecg/components/modal/UserSelectModal.vue';
  import DepartRoleUserAuthDrawer from './DepartRoleUserAuthDrawer.vue';
  import { departUserList, linkDepartUserBatch, unlinkDepartUserBatch } from '../depart.user.api';
  import { userInfoColumns, userInfoSearchFormSchema } from '../depart.user.data';
  import { ColEx } from '/@/components/Form/src/types';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  const userSelectModalRef: any = ref(null);
  // ID phòng ban đang chọn, có thể để trống, nghĩa là chưa chọn phòng ban
  const departId = computed(() => props.data?.id);

  // Cấu hình cột thích ứng
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 12, // ≥1200px
    xxl: 8, // ≥1600px
  };
  // Tham số và phương thức dùng chung của trang danh sách
  const { tableContext, createMessage } = useListPage({
    tableProps: {
      api: departUserList,
      columns: userInfoColumns,
      canResize: false,
      formConfig: {
        schemas: userInfoSearchFormSchema,
        baseColProps: adaptiveColProps,
        labelAlign: 'left',
        labelCol: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 9,
          xl: 7,
          xxl: 5,
        },
        wrapperCol: {},
        // Cấu hình nút thao tác
        actionColOptions: {
          ...adaptiveColProps,
          style: { textAlign: 'left' },
        },
        showResetButton: !!departId.value,
        showSubmitButton: !!departId.value,
      },
      // 【issues/1064】Thuộc về cài đặt cột cacheKey
      tableSetting: { cacheKey: 'depart_user_userInfo' },
      // Xử lý tham số trước khi gửi yêu cầu
      beforeFetch(params) {
        params.depId = departId.value;
      },
      // Giải thích logic mã: 【TV360X-1861】Khi không có phòng ban thì không tải thông tin người dùng
      immediate: !!departId.value,
    },
  });

  // Đăng ký ListTable
  const [registerTable, { reload, setProps, setLoading, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  watch(
    () => props.data,
    () => reload()
  );
  //Đăng kýdrawer
  const [registerDrawer, { openDrawer, setDrawerProps }] = useDrawer();
  const [registerUserAuthDrawer, userAuthDrawer] = useDrawer();
  // Chọn người dùng đăng ký modal
  const [registerSelUserModal, selUserModal] = useModal();

  // Bỏ chọn tất cả các hàng
  function clearSelection() {
    selectedRowKeys.value = [];
  }

  // Xem vai trò phòng ban
  function showDepartRole(record) {
    userAuthDrawer.openDrawer(true, { userId: record.id, departId });
  }

  // Tạo người dùng
  function createUser() {
    if (!departId.value) {
      createMessage.warning('Vui lòng chọn một phòng ban trước');
    } else {
      openDrawer(true, {
        isUpdate: false,
        departDisabled: true,
        // Khởi tạo phòng ban phụ trách
        nextDepartOptions: { value: props.data?.key, label: props.data?.title },
        record: {
          activitiSync: 1,
          userIdentity: 1,
          selecteddeparts: departId.value,
        },
      });
    }
  }

  // Xem chi tiết người dùng
  function showUserDetail(record) {
    openDrawer(true, {
      record,
      isUpdate: true,
      departDisabled: true,
      showFooter: false,
    });
  }

  // Chỉnh sửa thông tin người dùng
  function editUserInfo(record) {
    //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1141]【Quản lý hệ thống】Phòng ban và vai trò của người dùng không hiển thị khi chỉnh sửa tại mục Phòng ban của tôi------------
    openDrawer(true, { isUpdate: true, record, departDisabled: true, roleDisabled: true, departPostDisabled: true });
    //update-end---author:wangshuai ---date:20260811  for：[LHZP-1141]【Quản lý hệ thống】Phòng ban và vai trò của người dùng không hiển thị khi chỉnh sửa tại mục Phòng ban của tôi------------
  }

  // Chọn thêm người dùng hiện có
  function selectAddUser() {
    // Giải thích logic mã: 【TV360X-1613】Mở lại vẫn là người dùng đã chọn lần trước, chưa được đặt trống
    userSelectModalRef.value.rowSelection.selectedRowKeys = [];
    selUserModal.openModal();
  }

  // Hủy liên kết hàng loạt quan hệ giữa phòng ban và người dùng
  async function unlinkDepartUser(idList, confirm) {
    if (!departId.value) {
      createMessage.warning('Vui lòng chọn một phòng ban trước');
    } else {
      setLoading(true);
      let userIds = unref(idList).join(',');
      try {
        await unlinkDepartUserBatch({ depId: departId.value, userIds }, confirm);
        return reload();
      } finally {
        setLoading(false);
      }
    }
    return Promise.reject();
  }

  // Sự kiện hủy liên kết hàng loạt
  async function onUnlinkDepartUserBatch() {
    try {
      await unlinkDepartUser(selectedRowKeys, true);
      // Sau khi xóa hàng loạt thành công thì xóa danh sách đã chọn
      clearSelection();
    } catch (e) {}
  }

  // Chọn người dùng thành công
  async function onSelectUserOk(options, userIdList) {
    if (userIdList.length > 0) {
      try {
        setLoading(true);
        await linkDepartUserBatch(departId.value, userIdList);
        reload();
      } finally {
        setLoading(false);
      }
    }
  }

  /**
   * Callback thành công của biểu mẫu trong ngăn kéo người dùng
   */
  function onUserDrawerSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'Chỉnh sửa', onClick: editUserInfo.bind(null, record) }];
  }

  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'Vai trò phòng ban', onClick: showDepartRole.bind(null, record) },
      { label: 'Chi tiết người dùng', onClick: showUserDetail.bind(null, record) },
      {
        label: 'Hủy bỏ liên kết',
        color: 'error',
        popConfirm: {
          title: 'Xác nhận hủy liên kết?',
          confirm: unlinkDepartUser.bind(null, [record.id], false),
        },
      },
    ];
  }
</script>
