<template>
  <div class="p-4">
    <BasicTable @register="register">
      <template #toolbar>
        <a-button type="primary" @click="expandAll">Mở rộng tất cả</a-button>
        <a-button type="primary" @click="collapseAll">Thu gọn tất cả</a-button>
      </template>
    </BasicTable>
  </div>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';
  import { BasicColumn, BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  const columns: BasicColumn[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      fixed: 'left',
      width: 200,
    },
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 150,
      filters: [
        { text: 'Male', value: 'male' },
        { text: 'Female', value: 'female' },
      ],
    },
    {
      title: 'Địa chỉ',
      dataIndex: 'address',
      width: 300,
    },
    {
      title: 'Mã',
      dataIndex: 'no',
      width: 150,
      sorter: true,
      defaultHidden: true,
    },
    {
      title: 'Thời gian bắt đầu',
      width: 150,
      sorter: true,
      dataIndex: 'beginTime',
    },
    {
      title: 'Thời gian kết thúc',
      width: 150,
      sorter: true,
      dataIndex: 'endTime',
    },
  ];
  export default defineComponent({
    components: { BasicTable },
    setup() {
      const { tableContext } = useListPage({
        tableProps: {
          title: 'Bảng cây',
          isTreeTable: true,
          rowSelection: {
            type: 'checkbox',
            getCheckboxProps(record: Recordable) {
              // Demo: Ô chọn của dòng đầu tiên (id bằng 0) bị vô hiệu hóa
              if (record.id === '0') {
                return { disabled: true };
              } else {
                return { disabled: false };
              }
            },
          },
          columns: columns,
          dataSource: getTreeTableData(),
          rowKey: 'id',
          useSearchForm: false,
        },
      });
      //Đăng ký dữ liệu table
      const [register, { expandAll, collapseAll }] = tableContext;
      function getTreeTableData() {
        const data: any = (() => {
          const arr: any = [];
          for (let index = 0; index < 40; index++) {
            arr.push({
              id: `${index}`,
              name: 'John Brown',
              age: `1${index}`,
              no: `${index + 10}`,
              address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
              beginTime: new Date().toLocaleString(),
              endTime: new Date().toLocaleString(),
              children: [
                {
                  id: `l2-${index}`,
                  name: 'John Brown',
                  age: `1${index}`,
                  no: `${index + 10}`,
                  address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
                  beginTime: new Date().toLocaleString(),
                  endTime: new Date().toLocaleString(),
                },
                {
                  id: `l3-${index}`,
                  name: 'John Mary',
                  age: `1${index}`,
                  no: `${index + 10}`,
                  address: 'New York No. 1 Lake ParkNew York No. 1 Lake Park',
                  beginTime: new Date().toLocaleString(),
                  endTime: new Date().toLocaleString(),
                },
              ],
            });
          }
          return arr;
        })();

        return data;
      }
      return { register, expandAll, collapseAll };
    },
  });
</script>
