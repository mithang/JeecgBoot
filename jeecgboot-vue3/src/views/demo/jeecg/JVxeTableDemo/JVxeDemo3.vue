<template>
  <div>
    <ol style="border: 1px solid #cccccc; width: 600px; padding: 8px">
      <li>1. Bật thuộc tính dragSort là có thể kéo thả sắp xếp lên xuống.</li>
      <li>2. Dùng thuộc tính sortKey có thể tùy chỉnh key lưu sắp xếp, mặc định là orderNum.</li>
      <li>3. Dùng thuộc tính sortBegin có thể tùy chỉnh giá trị bắt đầu sắp xếp, mặc định là 0.</li>
      <li>4. sortKey Trường đã định nghĩa không cần định nghĩa trong columns vẫn có thể lấy giá trị bình thường.</li>
    </ol>

    <p> Ví dụ dưới đây đã bật sắp xếp kéo-thả, trường lưu giá trị sắp xếp là sortNum, giá trị bắt đầu sắp xếp là 3<br /> </p>

    <JVxeTable
      ref="tableRef1"
      toolbar
      dragSort
      sortKey="sortNum"
      :sortBegin="3"
      rowSelection
      dragSortFixed="none"
      rowSelectionFixed="none"
      :maxHeight="580"
      :columns="table1.columns"
      :dataSource="table1.data"
    >
      <template #toolbarSuffix>
        <a-button @click="onGetData1">Lấy dữ liệu</a-button>
      </template>
    </JVxeTable>

    <br />
    <p>Các bảng fixed dưới đây không hỗ trợ kéo thả sắp xếp, chỉ hỗ trợ nhấn sắp xếp lên/xuống</p>

    <JVxeTable ref="tableRef2" toolbar dragSort rowSelection :maxHeight="580" :columns="table2.columns" :dataSource="table2.data">
      <template #toolbarSuffix>
        <a-button @click="onGetData2">Lấy dữ liệu</a-button>
      </template>
    </JVxeTable>
  </div>
</template>

<script lang="ts" setup>
  import { ref, reactive } from 'vue';
  import { JVxeTypes, JVxeColumn, JVxeTableInstance } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  const tableRef1 = ref<JVxeTableInstance>();
  const tableRef2 = ref<JVxeTableInstance>();
  const table1 = reactive({
    columns: [
      {
        title: 'ID',
        key: 'id',
        width: 120,
        type: JVxeTypes.normal,
      },
      {
        title: 'Họ tên',
        key: 'name',
        width: 240,
        type: JVxeTypes.input,
        defaultValue: 'new name',
      },
      {
        title: 'Độ dài trường',
        key: 'dbLength',
        width: 2400,
        type: JVxeTypes.inputNumber,
        defaultValue: 32,
      },
      {
        title: 'sortNum',
        key: 'sortNum',
        width: 120,
        type: JVxeTypes.normal,
      },
    ] as JVxeColumn[],
    data: [
      { id: 'uuid-0001', name: 'Trương Tam', dbLength: 123 },
      { id: 'uuid-0002', name: 'Lý Tứ', dbLength: 777 },
      { id: 'uuid-0003', name: 'Vương Ngũ', dbLength: 666 },
      { id: 'uuid-0004', name: 'Triệu Lục', dbLength: 233 },
    ],
  });

  const table2 = reactive({
    columns: [
      {
        title: 'ID',
        key: 'id',
        width: 320,
        fixed: 'left',
        type: JVxeTypes.normal,
      },
      {
        title: 'Họ tên',
        key: 'name',
        width: 720,
        type: JVxeTypes.input,
        defaultValue: 'new name',
      },
      {
        title: 'Độ dài trường',
        key: 'dbLength',
        width: 720,
        type: JVxeTypes.inputNumber,
        defaultValue: 32,
      },
    ] as JVxeColumn[],
    data: [
      { id: 'uuid-0001', name: 'Trương Tam', dbLength: 123 },
      { id: 'uuid-0002', name: 'Lý Tứ', dbLength: 777 },
      { id: 'uuid-0003', name: 'Vương Ngũ', dbLength: 666 },
      { id: 'uuid-0004', name: 'Triệu Lục', dbLength: 233 },
    ],
  });

  const { createMessage } = useMessage();

  function onGetData1() {
    createMessage.info('Vui lòng xem bảng điều khiển (console)');
    console.log(tableRef1.value!.getTableData());
  }

  function onGetData2() {
    createMessage.info('Vui lòng xem bảng điều khiển (console)');
    console.log(tableRef2.value!.getTableData());
  }
</script>
