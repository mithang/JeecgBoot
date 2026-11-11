<template>
  <!--Tham chiếu bảng-->
  <BasicTable @register="registerTable" :rowSelection="rowSelection">
    <!--Khe cắm:table tiêu đề-->
    <template #tableTitle>
      <a-button type="primary" preIcon="ant-design:plus-outlined" @click="addDepartRole" :disabled="!departId">Thêm vai trò phòng ban</a-button>
      <template v-if="selectedRowKeys.length > 0">
        <a-divider type="vertical" />
        <a-dropdown>
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="onDeleteDepartRoleBatch">
                <icon icon="ant-design:delete-outlined" />
                <span>Xóa</span>
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
  <!-- Hộp thoại thêm phòng ban -->
  <DepartRoleModal :departId="departId" @register="registerFormModal" @success="onFormModalSuccess" />
  <DepartRoleAuthDrawer @register="registerAuthDrawer" />
</template>

<script lang="ts" setup>
  import { inject, ref, unref, watch, computed, onMounted } from 'vue';

  import { ActionItem, BasicTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import { useDrawer } from '/@/components/Drawer';
  import { useListPage } from '/@/hooks/system/useListPage';

  import DepartRoleModal from './DepartRoleModal.vue';
  import DepartRoleAuthDrawer from './DepartRoleAuthDrawer.vue';
  import { deleteBatchDepartRole, departRoleList } from '../depart.user.api';
  import { departRoleColumns, departRoleSearchFormSchema } from '../depart.user.data';
  import { ColEx } from '/@/components/Form/src/types';

  const prefixCls = inject('prefixCls');
  const props = defineProps({
    data: { require: true, type: Object },
  });
  defineEmits(['register']);
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
      api: departRoleList,
      columns: departRoleColumns,
      canResize: false,
      formConfig: {
        labelWidth: 100,
        schemas: departRoleSearchFormSchema,
        baseColProps: adaptiveColProps,
        labelAlign: 'left',
        labelCol: {
          xs: 24,
          sm: 24,
          md: 24,
          lg: 9,
          xl: 7,
          xxl: 6,
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
      tableSetting: { cacheKey: 'depart_user_departInfo' },
      // Xử lý tham số trước khi gửi yêu cầu
      beforeFetch(params) {
        params.deptId = departId.value;
      },
      // Giải thích logic mã: 【TV360X-53】Trong trường hợp chưa chọn phòng ban, vai trò phòng ban bị truy vấn ra toàn bộ
      immediate: !!departId.value,
    },
  });

  // Đăng ký ListTable
  const [registerTable, { reload, setProps, setLoading, updateTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

  // Hộp thoại Form đăng ký
  const [registerFormModal, formModal] = useModal();
  // Ngăn kéo hộp thoại cấp quyền đăng ký
  const [registerAuthDrawer, authDrawer] = useDrawer();

  // Theo dõi thay đổi của data, tải lại dữ liệu
  watch(
    () => props.data,
    () => reload()
  );
  onMounted(() => {
    // reload();
  });

  // Bỏ chọn tất cả các hàng
  function clearSelection() {
    selectedRowKeys.value = [];
  }

  // Thêm vai trò phòng ban
  function addDepartRole() {
    formModal.openModal(true, {
      isUpdate: false,
      record: {},
    });
  }

  // Chỉnh sửa vai trò phòng ban
  function editDepartRole(record) {
    formModal.openModal(true, {
      isUpdate: true,
      record: record,
    });
  }

  // Cấp quyền vai trò phòng ban
  function permissionDepartRole(record) {
    authDrawer.openDrawer(true, { record });
  }

  // Xóa hàng loạt vai trò phòng ban
  async function deleteDepartRole(idList, confirm) {
    if (!departId.value) {
      createMessage.warning('Vui lòng chọn một phòng ban trước');
    } else {
      setLoading(true);
      let ids = unref(idList).join(',');
      try {
        await deleteBatchDepartRole({ ids }, confirm);
        return reload();
      } finally {
        setLoading(false);
      }
    }
    return Promise.reject();
  }

  // Sự kiện xóa hàng loạt vai trò phòng ban
  async function onDeleteDepartRoleBatch() {
    try {
      await deleteDepartRole(selectedRowKeys, true);
      // Sau khi xóa hàng loạt thành công thì xóa danh sách đã chọn
      clearSelection();
    } catch (e) {}
  }

  // Callback sau khi hộp thoại biểu mẫu thành công
  function onFormModalSuccess({ isUpdate, values }) {
    isUpdate ? updateTableDataRecord(values.id, values) : reload();
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record): ActionItem[] {
    return [{ label: 'Chỉnh sửa', onClick: editDepartRole.bind(null, record) }];
  }

  /**
   * Thanh thao tác thả xuống
   */
  function getDropDownAction(record): ActionItem[] {
    return [
      { label: 'Cấp quyền', onClick: permissionDepartRole.bind(null, record) },
      {
        label: 'Xóa',
        color: 'error',
        popConfirm: {
          title: 'Bạn có chắc chắn muốn xóa không?',
          confirm: deleteDepartRole.bind(null, [record.id], false),
        },
      },
    ];
  }
</script>
