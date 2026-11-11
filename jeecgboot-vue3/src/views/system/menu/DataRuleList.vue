<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="Quy tắc quyền dữ liệu" :width="adaptiveWidth" :rootClassName="prefixCls">
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" @click="handleCreate"> Thêm mới</a-button>
      </template>
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicDrawer>
  <DataRuleModal @register="registerModal" @success="reload" :permissionId="permissionId" />
</template>
<script lang="ts" setup>
  import { ref, unref } from 'vue';
  import { BasicDrawer, useDrawerInner } from '/@/components/Drawer';
  import { BasicTable, useTable, TableAction } from '/@/components/Table';
  import { useModal } from '/@/components/Modal';
  import DataRuleModal from './DataRuleModal.vue';
  import { dataRuleColumns, dataRuleSearchFormSchema } from './menu.data';
  import { dataRuleList, deleteRule } from './menu.api';
  import { ColEx } from '/@/components/Form/src/types';
  import { useDrawerAdaptiveWidth } from '/@/hooks/jeecg/useAdaptiveWidth';
  import { useDesign } from '/@/hooks/web/useDesign';
  const permissionId = ref('');
  const { adaptiveWidth } = useDrawerAdaptiveWidth();
  const { prefixCls } = useDesign('sys-menu-dataRulelist');
  //Quy tắc quyền hạnmodel
  const [registerModal, { openModal }] = useModal();
  const [registerDrawer] = useDrawerInner(async (data) => {
    permissionId.value = data.id;
    setProps({ searchInfo: { permissionId: unref(permissionId) } });
    await getForm().resetFields();
    reload();
  });
  // Cấu hình cột thích ứng
  const adaptiveColProps: Partial<ColEx> = {
    xs: 24, // <576px
    sm: 24, // ≥576px
    md: 24, // ≥768px
    lg: 12, // ≥992px
    xl: 8, // ≥1200px
    xxl: 8, // ≥1600px
  };
  const [registerTable, { reload, setProps, getForm }] = useTable({
    api: dataRuleList,
    columns: dataRuleColumns,
    size: 'small',
    formConfig: {
      baseColProps: adaptiveColProps,
      labelAlign: 'right',
      labelCol: {
        offset: 1,
        xs: 24,
        sm: 24,
        md: 24,
        lg: 8,
        xl: 8,
        xxl: 8,
      },
      wrapperCol: {},
      schemas: dataRuleSearchFormSchema,
      autoSubmitOnEnter: true,
    },
    striped: true,
    useSearchForm: true,
    bordered: true,
    showIndexColumn: false,
    showTableSetting: false,
    canResize: false,
    immediate: false,
    actionColumn: {
      width: 100,
      title: 'Thao tác',
      dataIndex: 'action',
      slots: { customRender: 'action' },
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
    await deleteRule({ id: record.id }, reload);
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
</script>
<style lang="less">
  // -update-begin--author:liaozhiyang---date:20240702---for：【TV360X-1660】Quản lý menu - Ô truy vấn và nút của quyền dữ liệu không có khoảng cách
  @prefix-cls: ~'@{namespace}-sys-menu-dataRulelist';
  .@{prefix-cls} {
    .jeecg-basic-table {
      padding: 0;
    }
    .btnArea {
      .ant-btn {
        &:last-child {
          margin-right: 0;
        }
        &:first-child {
          margin-left: 8px;
        }
      }
    }
  }
  // -update-end--author:liaozhiyang---date:20240702---for：【TV360X-1660】Quản lý menu - Ô truy vấn và nút của quyền dữ liệu không có khoảng cách
</style>
