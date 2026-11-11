<template>
  <!--Tham chiếu bảng-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--Khe cắm:table tiêu đề-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="createUser" :disabled="!orgCode">Tạo mới người dùng</a-button>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="selectAddUser" :disabled="!orgCode || props.data?.orgCategory === '3'"
        >Thêm người dùng hiện có</a-button
      >
      <a-button v-if="selectedRowKeys.length > 0" preIcon="ant-design:user-delete-outlined" @click="onRemoveDepartUserBatch">Loại bỏ hàng loạt</a-button>
    </template>
    <!-- Slot: nút thao tác trong hàng -->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>
  </BasicTable>
  <UserDrawer @register="registerDrawer" @success="onUserDrawerSuccess" />
  <UserSelectModal ref="userSelectModalRef" rowKey="id" @register="registerSelUserModal" @getSelectResult="onSelectUserOk" />
</template>

<script lang="ts" setup>
  import { computed, h, inject, ref, watch } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { ActionItem, BasicColumn, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import UserDrawer from '/@/views/system/user/UserDrawer.vue';
  import UserSelectModal from '/@/components/Form/src/jeecg/components/modal/UserSelectModal.vue';
  import { queryDepartPostByOrgCode, removeDepartUser } from '../depart.api';
  import { ColEx } from '/@/components/Form/src/types';
  import { userColumns } from '@/views/system/depart/depart.data';
  import { linkDepartUserBatch } from '@/views/system/departUser/depart.user.api';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  const userSelectModalRef: any = ref(null);
  // Mã (code) phòng ban đang chọn, có thể trống, nghĩa là chưa chọn phòng ban
  const orgCode = computed(() => props.data?.orgCode);
  // Phòng ban hiện tạiid
  const departId = computed(() => props.data?.id);
  // Loại đang chọn: Phòng ban / Vị trí
  const departTypeText = computed(() => (props.data?.orgCategory === '3' ? 'Vị trí công tác' : 'Phòng ban'));
  const departUserColumns: BasicColumn[] = [
    {
      ...userColumns[0],
      // izCurrentDepartPost：Có phải người dùng thuộc phòng ban/vị trí hiện tại hay không (trong danh sách còn bao gồm người dùng của phòng ban, vị trí cấp dưới), nếu phải thì hiển thị 'Cấp này'
      customRender: ({ record, text }) => {
        if (record.izCurrentDepartPost !== true) {
          return h('span', [text]);
        }
        return h('span', [text, h(Tag, { color: 'blue', style: { marginLeft: '6px' } }, () => 'Cấp này')]);
      },
    },
    ...userColumns.slice(1),
  ];

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
  const { tableContext, createMessage, createConfirm } = useListPage({
    tableProps: {
      api: queryDepartPostByOrgCode,
      columns: departUserColumns,
      canResize: false,
      rowKey: 'id',
      formConfig: {
        // schemas: userInfoSearchFormSchema,
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
        // Cấu hình nút thao tác
        actionColOptions: {
          ...adaptiveColProps,
          style: { textAlign: 'left' },
        },
      },
      tableSetting: { cacheKey: 'depart_user_userInfo' },
      // Xử lý tham số trước khi gửi yêu cầu
      beforeFetch(params) {
        return Object.assign(params, { orgCode: orgCode.value });
      },
      immediate: !!orgCode.value,
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

  // Tạo người dùng
  async function createUser() {
    if (!departId.value) {
      createMessage.warning('Vui lòng chọn một phòng ban trước');
    } else {
      let mainDepPostId = '';
      let selecteddeparts = departId.value;
      if (props.data?.orgCategory === '3') {
        mainDepPostId = departId.value;
        selecteddeparts = props.data.parentId;
      }
      openDrawer(true, {
        isUpdate: false,
        //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1124]【Quản lý hệ thống】Quản lý phòng ban - Danh sách người dùng: khi chỉnh sửa người dùng tại đây, phòng ban và vai trò không hiển thị------------
        departDisabled: true,
        //update-end---author:wangshuai ---date:20260811  for：[LHZP-1124]【Quản lý hệ thống】Quản lý phòng ban - Danh sách người dùng: khi chỉnh sửa người dùng tại đây, phòng ban và vai trò không hiển thị------------
        // Khởi tạo phòng ban phụ trách
        nextDepartOptions: { value: props.data?.key, label: props.data?.title },
        //Khởi tạo vị trí
        record: {
          mainDepPostId: mainDepPostId,
          activitiSync: 1,
          userIdentity: 1,
          selecteddeparts: selecteddeparts,
        },
      });
    }
  }

  // Xem chi tiết người dùng
  function showUserDetail(record) {
    record.activitiSync = record.activitiSync? Number(record.activitiSync) : 1;
    openDrawer(true, {
      record,
      isUpdate: true,
      showFooter: false,
    });
  }

  // Chỉnh sửa thông tin người dùng
  function editUserInfo(record) {
    record.activitiSync = record.activitiSync? Number(record.activitiSync) : 1;
    //update-begin---author:wangshuai ---date:20260811  for：[LHZP-1124]【Quản lý hệ thống】Quản lý phòng ban - Danh sách người dùng: khi chỉnh sửa người dùng tại đây, phòng ban và vai trò không hiển thị------------
    openDrawer(true, { isUpdate: true, record, departDisabled: true, roleDisabled: true, departPostDisabled: true });
    //update-end---author:wangshuai ---date:20260811  for：[LHZP-1124]【Quản lý hệ thống】Quản lý phòng ban - Danh sách người dùng: khi chỉnh sửa người dùng tại đây, phòng ban và vai trò không hiển thị------------
  }

  // Chọn thêm người dùng hiện có
  function selectAddUser() {
    userSelectModalRef.value.rowSelection.selectedRowKeys = [];
    selUserModal.openModal();
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
   * Loại bỏ khỏi phòng ban/vị trí, chỉ xóa quan hệ liên kết, không xóa người dùng
   */
  async function onRemoveDepartUser(userIds: string[]) {
    try {
      setLoading(true);
      await removeDepartUser({ depId: departId.value, userIds: userIds.join(',') });
      clearSelection();
      reload();
    } catch (e) {
      // Thông tin thất bại đã được defHttp nhắc thống nhất
    } finally {
      setLoading(false);
    }
  }

  // Loại bỏ hàng loạt
  function onRemoveDepartUserBatch() {
    createConfirm({
      iconType: 'warning',
      title: 'Loại bỏ hàng loạt',
      content: `Bạn có chắc muốn loại bỏ ${selectedRowKeys.value.length} người dùng đã chọn khỏi ${departTypeText.value} hiện tại không?`,
      onOk: () => onRemoveDepartUser(selectedRowKeys.value),
    });
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
      { label: 'Chi tiết', onClick: showUserDetail.bind(null, record) },
      {
        label: 'Loại bỏ',
        color: 'error',
        // Chỉ người dùng thuộc phòng ban/vị trí hiện tại mới có thể loại bỏ, người dùng của phòng ban, vị trí cấp dưới cần thao tác tại phòng ban, vị trí tương ứng
        ifShow: record.izCurrentDepartPost === true,
        popConfirm: {
          title: `Xác nhận loại bỏ người dùng này khỏi ${departTypeText.value} hiện tại?`,
          confirm: onRemoveDepartUser.bind(null, [record.id]),
        },
      },
    ];
  }
</script>
