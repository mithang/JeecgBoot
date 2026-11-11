<!--Ô chọn người dùng dùng trong gói dịch vụ-->
<template>
  <div>
    <BasicModal
      v-bind="$attrs"
      @register="register"
      :title="modalTitle"
      width="900px"
      wrapClassName="j-user-select-modal"
      @ok="handleOk"
      destroyOnClose
    >
      <BasicTable ref="tableRef" @register="registerTable" :rowSelection="rowSelection">
        <template #tableTitle></template>
      </BasicTable>
    </BasicModal>
  </div>
</template>
<script lang="ts">
  import { defineComponent, ref } from 'vue';
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { getTenantUserList } from '../tenant.api';
  import { createAsyncComponent } from '/@/utils/factory/createAsyncComponent';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { userColumns, userSearchFormSchema } from '../tenant.data';

  export default defineComponent({
    name: 'TenantUserSelectModal',
    components: {
      //Tại đây cần tải không đồng bộBasicTable
      BasicModal,
      BasicTable: createAsyncComponent(() => import('/@/components/Table/src/BasicTable.vue'), {
        loading: true,
      }),
    },
    props: {
      //Tiêu đề ô chọn
      modalTitle: {
        type: String,
        default: 'Chọn người dùng',
      },
      //Tham số truy vấn
      tenantId: {
        type: Number,
        default: 0,
      },
      //Tập hợp các ID người dùng bị loại trừ
      excludeUserIdList: {
        type: Array,
        default: [],
      },
    },
    emits: ['register', 'on-select'],
    setup(props, { emit, refs }) {
      const tableScroll = ref<any>({ x: false });
      const tableRef = ref();
      //Hộp thoại đăng ký
      const [register, { closeModal }] = useModalInner(() => {
        if (window.innerWidth < 900) {
          tableScroll.value = { x: 900 };
        } else {
          tableScroll.value = { x: false };
        }
        setTimeout(() => {
          if (tableRef.value) {
            tableRef.value.setSelectedRowKeys([]);
          }
        }, 800);
      });

      //Định nghĩa cột bảng
      const columns = [
        {
          title: 'Tài khoản',
          dataIndex: 'username',
          width: 40,
          align: 'left',
        },
        {
          title: 'Họ tên',
          dataIndex: 'realname',
          width: 40,
        },
        {
          title: 'Giới tính',
          dataIndex: 'sex_dictText',
          width: 20,
        },
        {
          title: 'Số điện thoại',
          dataIndex: 'phone',
          width: 30,
        },
        {
          title: 'Email',
          dataIndex: 'email',
          width: 40,
        },
        {
          title: 'Trạng thái',
          dataIndex: 'status_dictText',
          width: 20,
        },
      ];

      // Tham số và phương thức dùng chung của trang danh sách
      const { prefixCls, tableContext } = useListPage({
        designScope: 'tenant-template',
        tableProps: {
          api: getTenantUserList,
          columns: userColumns,
          scroll: { y: 390 },
          rowKey: 'id',
          showActionColumn: false,
          formConfig: {
            schemas: userSearchFormSchema,
            labelWidth: 60,
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
            return Object.assign(params, { userTenantId: props.tenantId });
          },
        },
      });
      const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

      /**
       * Xác định lựa chọn
       */
      function handleOk() {
        //Sự kiện trả về mục đã chọn
        emit('on-select', rowSelection.selectedRows, rowSelection.selectedRowKeys);
      }

      return {
        handleOk,
        register,
        columns,
        rowSelection,
        tableScroll,
        tableRef,
        registerTable,
      };
    },
  });
</script>

<style scoped>
  :deep(.ant-input-suffix) {
    display: none;
  }
</style>
