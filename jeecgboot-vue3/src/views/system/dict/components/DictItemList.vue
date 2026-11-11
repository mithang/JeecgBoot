<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="Danh sách từ điển" width="800px">
    <BasicTable @register="registerTable" :rowClassName="getRowClassName">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate"> Thêm mới</a-button>
      </template>
      <template v-slot:bodyCell="{column, record, index}">
        <template v-if="column.dataIndex ==='action'">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </template>
     
    </BasicTable>
  </BasicDrawer>
  <DictItemModal @register="registerModal" @success="handleItemSuccess" :dictId="dictId" />
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/src/components/Drawer';
  import { BasicTable, useTable, TableAction } from '/src/components/Table';
  import { useModal } from '/src/components/Modal';
  import { useDesign } from '/@/hooks/web/useDesign';
  import DictItemModal from './DictItemModal.vue';
  import { dictItemColumns, dictItemSearchFormSchema } from '../dict.data';
  import { itemList, deleteItem, refreshCache, queryAllDictItems } from '../dict.api';
  import { ColEx } from '/@/components/Form/src/types';
  import { useMessage } from '/src/hooks/web/useMessage';
  import { removeAuthCache } from '/src/utils/auth';
  import { DB_DICT_DATA_KEY } from '/src/enums/cacheEnum';
  import { useUserStore } from '/@/store/modules/user';

  const { prefixCls } = useDesign('row-invalid');
  const { createMessage } = useMessage();
  const dictId = ref('');
  //Cấu hình từ điểnmodel
  const [registerModal, { openModal }] = useModal();
  const [registerDrawer] = useDrawerInner(async (data) => {
    dictId.value = data.id;
    setProps({ searchInfo: { dictId: unref(dictId) } });
    reload();
  });
  // Cấu hình cột thích ứng
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 12, // ≥1200px
    xxl: 8, // ≥1600px
  };
  const [registerTable, { reload, setProps }] = useTable({
    //Cần cấu hình rowKey, nếu không sẽ có cảnh báo
    rowKey:'dictId',
    api: itemList,
    columns: dictItemColumns,
    formConfig: {
      baseColProps: adaptiveColProps,
      labelAlign: 'right',
      labelCol: {
        offset: 1,
        xs: 24,
        sm: 24,
        md: 24,
        lg: 9,
        xl: 7,
        xxl: 4,
      },
      wrapperCol: {},
      schemas: dictItemSearchFormSchema,
      autoSubmitOnEnter: true,
      actionColOptions: {
        span: 8
      }
    },
    striped: true,
    useSearchForm: true,
    bordered: true,
    showIndexColumn: false,
    canResize: false,
    immediate: false,
    actionColumn: {
      width: 100,
      title: 'Thao tác',
      dataIndex: 'action',
      //slots: { customRender: 'action' },
      fixed: undefined,
    },
  });

  /**
   * Thêm mới
   */
  function handleCreate() {
    openModal(true, {
      isUpdate: false,
    });
  }

  /**
   * Chỉnh sửa
   */
  function handleEdit(record) {
    openModal(true, {
      record,
      isUpdate: true,
    });
  }

  /**
   * Xóa
   */
  async function handleDelete(record) {
    await deleteItem({ id: record.id }, () => {
      reload();
      doRefreshDictCache();
    });
  }

  /**
   * Thêm mới/chỉnh sửa mục từ điển thành công: làm mới danh sách + đồng bộ làm mới bộ nhớ đệm từ điển
   */
  async function handleItemSuccess() {
    await reload();
    await doRefreshDictCache();
  }

  /**
   * Làm mới bộ nhớ đệm từ điển (bộ nhớ đệm backend + store frontend + bộ nhớ đệm cục bộ)
   */
  async function doRefreshDictCache() {
    try {
      const result = await refreshCache();
      if (result && result.success) {
        const res = await queryAllDictItems();
        removeAuthCache(DB_DICT_DATA_KEY);
        // Giải thích logic mã: 【QQYUN-6417】Sự cố từ điển chậm trong môi trường production
        const userStore = useUserStore();
        userStore.setAllDictItems(res.result);
        //createMessage.success('Làm mới bộ nhớ đệm từ điển thành công!');
      } else {
       // createMessage.error('Làm mới bộ nhớ đệm từ điển thất bại!');
      }
    } catch (e) {
      //createMessage.error('Làm mới bộ nhớ đệm từ điển bất thường!');
    }
  }

  /**
   * Thanh thao tác
   */
  function getTableAction(record) {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
      {
        label: 'Xóa',
        popConfirm: {
          title: 'Xác nhận xóa?',
          confirm: handleDelete.bind(null, record),
        },
      },
    ];
  }
  function getRowClassName(record) {
    return record.status == 0 ? prefixCls : '';
  }
</script>
<style scoped lang="less">
  @prefix-cls: ~'@{namespace}-row-invalid';

  :deep(.@{prefix-cls}) {
    background: #f4f4f4;
    color: #bababa;
  }
</style>
