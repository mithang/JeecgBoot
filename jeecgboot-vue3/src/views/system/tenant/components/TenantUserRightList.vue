<template>
  <BasicTable @register="registerTable" style="padding-top: 10px">
    <template #departNames="{ text, record }">
      <template v-if="text && text.length > 0">
        {{ getName(text) }}
      </template>
    </template>
    <template #positionNames="{ text, record }">
      <template v-if="text && text.length > 0">
        {{ getName(text) }}
      </template>
    </template>
    <template #tableTitle>
      <a-button preIcon="ant-design:usergroup-add-outlined" type="primary" @click="addUser" :disabled="tenantPackData.izSysn == '1'">Mời thành viên</a-button>
      <a-button preIcon="ant-design:rollback-outlined" @click="cancel">Đóng</a-button>
    </template>
    <!--Thanh thao tác-->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>
  </BasicTable>
  <tenant-user-select-modal :multi="true" @register="registerUserModal" @on-select="onSelected" :tenantId="getTenantId"></tenant-user-select-modal>
</template>

<script lang="ts">
  import { defineComponent, defineEmits } from 'vue';
  import { BasicModal } from '@/components/Modal';
  import { BasicTable, TableAction } from '@/components/Table';
  import TenantUserSelectModal from '@/views/system/tenant/components/TenantUserSelectModal.vue';
  import { addTenantPackUser, deleteTenantPackUser } from '@/views/system/tenant/tenant.api';
  //Đăng ký dữ liệu table
  import { useListPage } from '@/hooks/system/useListPage';
  import { queryTenantPackUserList } from '@/views/system/tenant/tenant.api';
  import { tenantPackUserColumns } from '@/views/system/tenant/tenant.data';
  import { useModal } from '@/components/Modal';
  import { computed, reactive } from 'vue';

  export default defineComponent({
    name: 'TenantUserRightList',
    components: { BasicModal, BasicTable, TableAction, TenantUserSelectModal },
    emits: ['cancel'],
    setup(props, { emit }) {
      const [registerUserModal, { openModal: openUserModal, closeModal: closeUserModal }] = useModal();
      //Lấy đơn vị thuêid
      const getTenantId = computed(() => {
        return tenantPackData.tenantId;
      });

      //Thông tin gói dịch vụ
      const tenantPackData = reactive<any>({});
      const { tableContext } = useListPage({
        tableProps: {
          api: queryTenantPackUserList,
          immediate: false,
          columns: tenantPackUserColumns,
          canResize: false,
          useSearchForm: false,
          beforeFetch: (params) => {
            params.tenantId = tenantPackData.tenantId;
            params.packId = tenantPackData.id;
            params.status = 1;
            return params;
          },
          actionColumn: {
            width: 120,
            fixed: 'right',
          },
        },
      });

      const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

      /**
       * Khởi tạo dữ liệu
       * @param value
       */
      function initData(value) {
        console.log('value：：', value);
        Object.assign(tenantPackData, value);
        reload();
      }

      /**
       * Cột thao tác của bảng
       * @param record
       */
      function getTableAction(record) {
        return [
          {
            label: 'Loại bỏ',
            popConfirm: {
              title: 'Xác nhận loại bỏ?',
              confirm: handleDelete.bind(null, record),
            },
            ifShow:() => tenantPackData.izSysn != '1'
          },
        ];
      }

      /**
       * Xóa
       */
      async function handleDelete(record) {
        let params = {
          packId: record.packId,
          packName: record.packName,
          tenantId: tenantPackData.tenantId,
          userId: record.id,
          realname: record.realname,
        };
        await deleteTenantPackUser(params);
        await reload();
      }

      /**
       * Hộp thoại thêm người dùng
       */
      function addUser() {
        openUserModal(true, {
          list: [],
        });
      }

      /**
       * Sự kiện callback của người mời
       * @param arr
       */
      async function onSelected(arr) {
        if (arr && arr.length > 0) {
          let names: any[] = [];
          let ids: any[] = [];
          for (let u of arr) {
            names.push(u.realname);
            ids.push(u.id);
          }
          let params = {
            packId: tenantPackData.id,
            packName: tenantPackData.packName,
            tenantId: tenantPackData.tenantId,
            userId: ids.join(','),
            realname: names.join(','),
          };
          await addTenantPackUser(params);
          await reload();
          closeUserModal();
        }
      }

      /**
       * Lấy tên phòng ban/chức danh
       * @param value
       */
      function getName(value) {
        return value.join(',');
      }

      /**
       * Đóng
       */
      function cancel() {
        emit('cancel');
      }

      return {
        getTableAction,
        onSelected,
        registerTable,
        registerUserModal,
        getTenantId,
        rowSelection,
        addUser,
        initData,
        getName,
        cancel,
        tenantPackData,
      };
    },
  });
</script>

<style scoped lang="less"></style>
