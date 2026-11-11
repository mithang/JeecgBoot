<template>
  <div class="p-4">
    <!--Định nghĩa bảng-->
    <BasicTable @register="registerTable">
      <!-- Slot khu vực tìm kiếm tùy chỉnh truy vấn -->
      <template #form-email="{ model, field }">
        <a-input placeholder="Vui lòng nhập email" v-model:value="model[field]" addon-before="Email:" addon-after=".com"></a-input>
      </template>
      <!--Thanh thao tác-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="basic-table-demo" setup>
  import { ActionItem, BasicColumn, BasicTable, FormSchema, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { defHttp } from '/@/utils/http/axios';
  //Định nghĩa cột bảng
  const columns: BasicColumn[] = [
    {
      title: 'Họ tên',
      dataIndex: 'name',
      width: 170,
      align: 'left',
      resizable: true,
      sorter: {
        multiple: 1,
      },
    },
    {
      title: 'Từ khóa',
      dataIndex: 'keyWord',
      width: 130,
      resizable: true,
    },
    {
      title: 'Thời gian chấm công',
      dataIndex: 'punchTime',
      width: 140,
      resizable: true,
    },
    {
      title: 'Lương',
      dataIndex: 'salaryMoney',
      width: 140,
      resizable: true,
      sorter: {
        multiple: 2,
      },
    },
    {
      title: 'Tiền thưởng',
      dataIndex: 'bonusMoney',
      width: 140,
      resizable: true,
    },
    {
      title: 'Giới tính',
      dataIndex: 'sex',
      sorter: {
        multiple: 3,
      },
      filters: [
        { text: 'Nam', value: '1' },
        { text: 'Nữ', value: '2' },
      ],
      customRender: ({ record }) => {
        return record.sex ? (record.sex == '1' ? 'Nam' : 'Nữ') : '';
      },
      width: 120,
      resizable: true,
    },
    {
      title: 'Ngày sinh',
      dataIndex: 'birthday',
      width: 120,
      resizable: true,
    },
    {
      title: 'Email',
      dataIndex: 'email',
      width: 120,
      resizable: true,
    },
  ];
  //Trường tìm kiếm biểu mẫu
  const searchFormSchema: FormSchema[] = [
    {
      label: 'Họ tên', //Hiển thịlabel
      field: 'name', //Trường tìm kiếm
      component: 'JInput', //Component được kết xuất
      defaultValue: 'Tô Dung Nhuận', //Đặt giá trị mặc định
    },
    {
      label: 'Giới tính',
      field: 'sex',
      component: 'JDictSelectTag',
      componentProps: {
        //của component được renderprops
        dictCode: 'sex',
        placeholder: 'Vui lòng chọn giới tính',
      },
    },
    {
      label: 'Email',
      field: 'email',
      component: 'JInput',
      slot: 'email',
    },
    {
      label: 'Ngày sinh',
      field: 'birthday',
      component: 'DatePicker',
    },
  ];
  //ajaxYêu cầu API
  const demoListApi = (params) => {
    return defHttp.get({ url: '/test/jeecgDemo/list', params });
  };
  // Tham số, phương thức chung của trang danh sách
  const { tableContext } = useListPage({
    designScope: 'basic-table-demo-filter',
    tableProps: {
      title: 'Danh sách người dùng',
      api: demoListApi,
      columns: columns,
      formConfig: {
        schemas: searchFormSchema,
      },
      useSearchForm: true,
    },
  });
  //BasicTableĐăng ký liên kết
  const [registerTable, { getForm }] = tableContext;
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
    let { getFieldsValue } = getForm();
    console.log('Truy vấn dữ liệu của form', getFieldsValue());
    console.log(record);
  }
</script>

<style scoped></style>
