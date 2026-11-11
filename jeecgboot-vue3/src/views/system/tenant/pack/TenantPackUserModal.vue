<template>
  <BasicModal @register="registerModal" destroyOnClose :title="title" :width="1000" :footer="null">
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
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
        <a-button preIcon="ant-design:usergroup-add-outlined" type="primary" @click="addUser">Mời thành viên</a-button>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <tenant-user-select-modal :multi="true" @register="registerUserModal" @on-select="onSelected" :tenantId="getTenantId"></tenant-user-select-modal>
  </BasicModal>
</template>

<script lang="ts">
  import { computed, defineComponent, reactive, ref } from 'vue';
  import { BasicModal, useModal, useModalInner } from '/@/components/Modal';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { tenantPackUserColumns } from '../tenant.data';
  import { queryTenantPackUserList, deleteTenantPackUser, addTenantPackUser } from '../tenant.api';
  import TenantUserSelectModal from '../components/TenantUserSelectModal.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';

  export default defineComponent({
    name: 'TenantPackUserModal',
    components: { BasicModal, BasicTable, TableAction, TenantUserSelectModal },
    setup() {
      //Lấy đơn vị thuêid
      const getTenantId = computed(()=>{
        return tenantPackData.tenantId;
      })
      
      //Thông tin gói dịch vụ
      const tenantPackData = reactive<any>({});
      //Gán giá trị cho biểu mẫu
      const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
        setModalProps({ confirmLoading: false, showCancelBtn: true, showOkBtn: false });
        Object.assign(tenantPackData, data.record);
        await reload();
      });
      const { createMessage } = useMessage();
      //Đặt tiêu đề
      const title = ref<string>('Người dùng');
      //Đăng ký dữ liệu table
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
      const [registerUserModal, { openModal: openUserModal, closeModal: closeUserModal }] = useModal();
      const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

      /**
       * Lấy tên phòng ban/chức danh
       * @param value
       */
      function getName(value) {
        return value.join(',');
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
            }
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
          console.log(tenantPackData);
          let params = {
            packId: tenantPackData.id,
            packName: tenantPackData.packName,
            tenantId: tenantPackData.tenantId,
            userId: ids.join(','),
            realname: names.join(','),
          };
          await addTenantPackUser(params);
          await reload();
        }
        closeUserModal();
      }

      return {
        title,
        registerModal,
        registerTable,
        rowSelection,
        getName,
        getTableAction,
        registerUserModal,
        addUser,
        onSelected,
        getTenantId,
      };
    },
  });
</script>

<style lang="less" scoped></style>
