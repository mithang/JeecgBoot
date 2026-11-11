<template>
  <div class="p-4">
    <!--Bảng tham chiếu-->
    <BasicTable @register="registerTable">
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="basic-table-demo" setup>
  import { ActionItem, BasicColumn, BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  //Định nghĩa cột bảng
  const columns: BasicColumn[] = [
    {
      title: 'Tên',
      dataIndex: 'name',
      customCell: (record, index, column) => ({
        colSpan: index < 4 ? 1 : 5,
      }),
      customRender: ({ text, record, index, column }) => {
        return index < 4 ? text : `${record.name}/${record.age}/${record.address}/${record.phone}`;
      },
    },
    {
      title: 'Tuổi',
      dataIndex: 'age',
      customCell: (record, index, column) => {
        if (index == 4) {
          return { colSpan: 0 };
        }
      },
    },
    {
      title: 'Địa chỉ gia đình',
      dataIndex: 'address',
      customCell: (record, index, column) => {
        if (index == 4) {
          return { colSpan: 0 };
        }
      },
    },
    {
      title: 'Số điện thoại liên hệ',
      colSpan: 2,
      dataIndex: 'tel',
      customCell: (record, index, column) => {
        if (index === 2) {
          return { rowSpan: 2 };
        }
        if (index === 3) {
          return { rowSpan: 0 };
        }
        if (index === 4) {
          return { colSpan: 0 };
        }
      },
    },
    {
      title: 'Phone',
      colSpan: 0,
      dataIndex: 'phone',
      customCell: (record, index, column) => {
        if (index === 4) {
          return { colSpan: 0 };
        }
      },
    },
  ];
  // Tham số, phương thức chung của trang danh sách
  const { tableContext } = useListPage({
    designScope: 'basic-table-demo',
    tableProps: {
      title: 'Gộp hàng và cột',
      dataSource: [
        {
          key: '1',
          name: 'Doãn Gia Lạc',
          age: 32,
          tel: '0319-5972018',
          phone: 17600080009,
          address: 'Quận Xương Bình, Bắc Kinh',
        },
        {
          key: '2',
          name: 'Long Giai Ngọc',
          tel: '0319-5972018',
          phone: 17600060007,
          age: 42,
          address: 'Quận Hải Điến, Bắc Kinh',
        },
        {
          key: '3',
          name: 'Hạ Trạch Huệ',
          age: 32,
          tel: '0319-5972018',
          phone: 17600040005,
          address: 'Quận Môn Đầu Câu, thành phố Bắc Kinh',
        },
        {
          key: '4',
          name: 'Thẩm Dũng',
          age: 18,
          tel: '0319-5972018',
          phone: 17600010003,
          address: 'Quận Triều Dương, Bắc Kinh',
        },
        {
          key: '5',
          name: 'Bạch Giai Nghị',
          age: 18,
          tel: '0319-5972018',
          phone: 17600010002,
          address: 'Quận Phong Đài, Bắc Kinh',
        },
      ],
      columns: columns,
      showActionColumn: false,
      useSearchForm: false,
    },
  });
  //Đăng ký dữ liệu table
  const [registerTable] = tableContext;
  /**
   * Thanh thao tác
   */
  function getTableAction(record): ActionItem[] {
    return [
      {
        label: 'Chỉnh sửa',
        onClick: handleEdit.bind(null, record),
      },
    ];
  }

  function handleEdit(record) {
    console.log(record);
  }
</script>

<style scoped></style>
