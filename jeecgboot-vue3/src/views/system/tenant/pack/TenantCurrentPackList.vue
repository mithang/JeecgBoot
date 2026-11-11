<!-- Gói dịch vụ thuộc đơn vị thuê hiện tại -->
<template>
  <div style="display: flex; width: 100%">
    <div :style="leftStyle">
      <BasicTable @register="registerTable">
        <template #tableTitle>
          <div style="margin-left: 10px; margin-top: 5px"
            >Đơn vị thuê đang đăng nhập: <span class="tenant-name">{{ loginTenantName }}</span>
          </div>
        </template>
        <!--Thanh thao tác-->
        <template #action="{ record }">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </BasicTable>
    </div>
    <div v-if="showRight" style="width: 49%; transition: width 0.3s">
      <TenantUserRightList ref="rightListRef" @cancel="handleCancel"></TenantUserRightList>
    </div>
  </div>
  <TenantPackMenuModal @register="registerPackMenu" @success="success" />
</template>
<script lang="ts">
  import { ref, unref, defineComponent, defineEmits } from 'vue';
  import { useModal } from '/@/components/Modal';
  import { packFormSchema, tenantPackColumns } from '../tenant.data';
  import { getLoginTenantName, packList } from '../tenant.api';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { BasicTable, TableAction } from '/@/components/Table';
  import TenantPackMenuModal from './TenantPackMenuModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getTenantId } from '/@/utils/auth';
  import TenantUserRightList from '../components/TenantUserRightList.vue';

  export default defineComponent({
    name: 'TenantCurrentPackList',
    components: {
      TenantUserRightList,
      BasicTable,
      TableAction,
      TenantPackMenuModal,
    },
    setup() {
      const [registerPackMenu, { openModal }] = useModal();
      const [registerPackUser, { openModal: packUserOpenModal }] = useModal();

      const tenantId = ref<number>();
      const rightListRef = ref<any>();
      //Có hiển thị danh sách người dùng bên phải hay không
      const showRight = ref<any>();
      //Kiểu phía bên trái
      const leftStyle = ref<any>({
        width: '100%',
        transition: 'width 0.3s',
      });

      // Tham số và phương thức dùng chung của trang danh sách
      const { prefixCls, tableContext } = useListPage({
        designScope: 'tenant-template',
        tableProps: {
          api: packList,
          columns: tenantPackColumns,
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
            return Object.assign(params, { tenantId: tenantId.value, status: '1', packType: 'custom' });
          },
        },
      });
      const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;
      //Tên đơn vị thuê đang đăng nhập
      const loginTenantName = ref<string>('');
      const { createMessage } = useMessage();

      /**
       * Lấy tên đơn vị thuê
       */
      getTenantName();

      async function getTenantName() {
        let id = getTenantId();
        if (id) {
          loginTenantName.value = await getLoginTenantName();
          tenantId.value = Number(id);
          await reload();
        }
      }

      /**
       * Thao tác bảng
       * @param record
       */
      function getTableAction(record) {
        return [
          {
            label: 'Người dùng',
            onClick: seeTenantPackUser.bind(null, record),
          },
          {
            label: 'Chi tiết gói dịch vụ',
            onClick: handleDetail.bind(null, record),
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
       * Người dùng thuộc gói dịch vụ
       * @param record
       */
      function seeTenantPackUser(record) {
        showRight.value = true;
        leftStyle.value = {
          width: '50%',
          transition: 'width 0.3s',
        };
        setTimeout(() => {
          rightListRef.value.initData(record);
        }, 300);
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
          packType: 'custom',
          showFooter: false,
        });
      }

      /**
       * Callback khi đóng
       */
      function handleCancel() {
        showRight.value = false;
        leftStyle.value = {
          width: '100%',
          transition: 'width 0.3s',
        };
      }

      return {
        registerPackMenu,
        success,
        getTableAction,
        registerTable,
        rowSelection,
        rightListRef,
        showRight,
        registerPackUser,
        loginTenantName,
        leftStyle,
        handleCancel,
      };
    },
  });
</script>
<style lang="less" scoped>
  .tenant-name {
    text-decoration: underline;
    margin: 5px;
    font-size: 15px;
  }
</style>
