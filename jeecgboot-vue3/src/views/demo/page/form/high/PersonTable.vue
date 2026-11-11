<template>
  <div>
    <BasicTable @register="registerTable" @edit-change="handleEditChange">
      <template #action="{ record, column }">
        <TableAction :actions="createActions(record, column)" />
      </template>
    </BasicTable>
    <a-button block class="mt-5" type="dashed" @click="handleAdd"> Thêm mới thành viên </a-button>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicTable, useTable, TableAction, BasicColumn, ActionItem, EditRecordRow } from '/@/components/Table';

  const columns: BasicColumn[] = [
    {
      title: 'Tên thành viên',
      dataIndex: 'name',
      editRow: true,
    },
    {
      title: 'Mã nhân viên',
      dataIndex: 'no',
      editRow: true,
    },
    {
      title: 'Phòng ban trực thuộc',
      dataIndex: 'dept',
      editRow: true,
    },
  ];

  const data: any[] = [
    {
      name: 'John Brown',
      no: '00001',
      dept: 'New York No. 1 Lake Park',
    },
    {
      name: 'John Brown2',
      no: '00002',
      dept: 'New York No. 2 Lake Park',
    },
    {
      name: 'John Brown3',
      no: '00003',
      dept: 'New York No. 3Lake Park',
    },
  ];
  export default defineComponent({
    components: { BasicTable, TableAction },
    setup() {
      const [registerTable, { getDataSource }] = useTable({
        columns: columns,
        showIndexColumn: false,
        dataSource: data,
        actionColumn: {
          width: 160,
          title: 'Thao tác',
          dataIndex: 'action',
          slots: { customRender: 'action' },
        },
        pagination: false,
      });

      function handleEdit(record: EditRecordRow) {
        record.onEdit?.(true);
      }

      function handleCancel(record: EditRecordRow) {
        record.onEdit?.(false);
        if (record.isNew) {
          const data = getDataSource();
          const index = data.findIndex((item) => item.key === record.key);
          data.splice(index, 1);
        }
      }

      function handleSave(record: EditRecordRow) {
        record.onEdit?.(false, true);
      }

      function handleEditChange(data: Recordable) {
        console.log(data);
      }

      function handleAdd() {
        const data = getDataSource();
        const addRow: EditRecordRow = {
          name: '',
          no: '',
          dept: '',
          editable: true,
          isNew: true,
          key: `${Date.now()}`,
        };
        data.push(addRow);
      }

      function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
        if (!record.editable) {
          return [
            {
              label: 'Chỉnh sửa',
              onClick: handleEdit.bind(null, record),
            },
            {
              label: 'Xóa',
            },
          ];
        }
        return [
          {
            label: 'Lưu',
            onClick: handleSave.bind(null, record, column),
          },
          {
            label: 'Hủy',
            popConfirm: {
              title: 'Có hủy chỉnh sửa không',
              confirm: handleCancel.bind(null, record, column),
            },
          },
        ];
      }

      return {
        registerTable,
        handleEdit,
        createActions,
        handleAdd,
        getDataSource,
        handleEditChange,
      };
    },
  });
</script>
