<template>
  <div>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #tableTitle>
        <a-button preIcon="ant-design:swap-outlined" type="primary" size="small" @click="handleCompare">So sánh dữ liệu</a-button>
        <span v-if="selectedRowKeys.length === 0" class="compare-tip">Vui lòng chọn hai bản ghi có cùng ID dữ liệu</span>
        <a-tag v-else-if="selectedRowKeys.length === 1" color="warning" style="margin-left: 10px">Chọn thêm một bản ghi có cùng ID dữ liệu</a-tag>
        <a-tag v-else-if="selectedRowKeys.length === 2 && !isSameDataId" color="error" style="margin-left: 10px">ID dữ liệu không nhất quán, không thể so sánh</a-tag>
        <a-tag v-else-if="selectedRowKeys.length === 2 && isSameDataId" color="success" style="margin-left: 10px">Có thể so sánh V{{ selectedRows[0]?.dataVersion }} vs V{{ selectedRows[1]?.dataVersion }}</a-tag>
        <a-tag v-else color="error" style="margin-left: 10px">Chỉ có thể chọn hai bản ghi</a-tag>
      </template>
    </BasicTable>
    <DataLogCompareModal @register="registerModal" @success="reload" />
  </div>
</template>
<script lang="ts" name="monitor-datalog" setup>
  import { computed } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import DataLogCompareModal from './DataLogCompareModal.vue';
  import { getDataLogList } from './datalog.api';
  import { columns, searchFormSchema } from './datalog.data';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useModal } from '/@/components/Modal';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getTableList } from '/@/api/common/api';

  const [registerModal, { openModal }] = useModal();
  const { createMessage } = useMessage();

  const { tableContext } = useListPage({
    designScope: 'datalog-template',
    tableProps: {
      title: 'Danh sách nhật ký dữ liệu',
      api: getDataLogList,
      columns: columns,
      afterFetch: translateCreateBy,
      formConfig: {
        labelWidth: 80,
        schemas: searchFormSchema,
      },
      actionColumn: false,
    },
  });

  const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

  const isSameDataId = computed(() => {
    const rows = selectedRows.value;
    if (!rows || rows.length !== 2) return false;
    return rows[0].dataId === rows[1].dataId;
  });

  /** Dịch hàng loạt tài khoản người tạo thành tên người dùng */
  async function translateCreateBy(records) {
    const usernames = [...new Set(records?.map((item) => item.createBy).filter(Boolean))];
    if (!usernames.length) {
      return records;
    }
    try {
      const userInfo = await getTableList({ username: usernames.join(','), isMultiTranslate: true });
      const realnameMap = new Map(userInfo?.records?.map((item) => [item.username, item.realname]));
      records.forEach((record) => {
        record.createBy = realnameMap.get(record.createBy) || record.createBy;
      });
    } catch (_error) {
      // Khi dịch thất bại, giữ lại tài khoản gốc, không ảnh hưởng đến việc hiển thị danh sách nhật ký
    }
    return records;
  }

  function handleCompare() {
    const rows = selectedRows.value;
    if (!rows || rows.length !== 2) {
      createMessage.warning('Vui lòng chọn hai dữ liệu để so sánh!');
      return;
    }
    if (rows[0].dataId !== rows[1].dataId) {
      createMessage.warning('Vui lòng chọn các bản ghi có cùng ID dữ liệu để so sánh!');
      return;
    }
    openModal(true, {
      selectedRows: rows,
      isUpdate: true,
    });
  }
</script>
<style lang="less" scoped>
  .compare-tip {
    margin-left: 10px;
    font-size: 12px;
    color: #999;
  }
</style>
