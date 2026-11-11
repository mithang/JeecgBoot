<template>
  <a-card title="Ví dụ bảng con bật lên" :bordered="false">
    <!--
      [Ý tưởng tổng thể về bảng con bật lên]
      1. Bắt buộc phải có thuộc tính clickRowShowSubForm, nếu thuộc tính này được đặt là false thì sẽ không bật lên bảng con
      2. Bắt buộc phải có slot subForm, dùng để quy định nội dung bảng con bật lên
      3. highlightCurrentRow Thuộc tính có hay không cũng được, nếu có thì khi nhấn vào một dòng, màu nền của dòng đó sẽ luôn sáng
    -->
    <!--
      [Ý tưởng chung về popup thông tin chi tiết]
      1. Bắt buộc phải có thuộc tính clickRowShowMainForm, nếu thuộc tính này được đặt là false thì sẽ không bật lên thông tin chi tiết
      2. Bắt buộc phải có slot mainForm, dùng để quy định nội dung bật lên
    -->
    <JVxeTable
      toolbar
      rowNumber
      rowSelection
      highlightCurrentRow
      clickRowShowSubForm
      clickRowShowMainForm
      :height="750"
      :loading="loading"
      :columns="columns"
      :dataSource="dataSource"
      @detailsConfirm="handleDetailsConfirm"
    >
      <!-- Biểu mẫu chính -->
      <template #mainForm="{ row }">
        <template v-if="row">
          <a-form ref="form2" :model="row" :rules="rules" :label-col="labelCol" :wrapper-col="wrapperCol">
            <a-row :gutter="8">
              <a-col :span="8">
                <a-form-item label="ID" name="id">
                  <a-input v-model:value="row.id" disabled />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="STT" name="num">
                  <a-input v-model:value="row.num" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="Tên tàu" name="ship_name">
                  <a-input v-model:value="row.ship_name" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="Gọi" name="call">
                  <a-input v-model:value="row.call" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="Chiều dài" name="len">
                  <a-input v-model:value="row.len" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="tấn" name="ton">
                  <a-input v-model:value="row.ton" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="Bên thanh toán" name="payer">
                  <a-input v-model:value="row.payer" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="Số" name="count">
                  <a-input v-model:value="row.count" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="Công ty" name="company">
                  <a-input v-model:value="row.company" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="Xu hướng" name="trend">
                  <a-input v-model:value="row.trend" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </template>
      </template>

      <!-- Biểu mẫu con -->
      <template #subForm="{ row }">
        <template v-if="loadSubData(row)">
          <JVxeTable
            ref="subFormTable"
            height="auto"
            :max-height="350"
            :loading="subTable.loading"
            :columns="subTable.columns"
            :dataSource="subTable.dataSource"
          />
        </template>
      </template>
    </JVxeTable>
  </a-card>
</template>

<script lang="ts" setup>
  // Ví dụ bảng con bật lên
  import { reactive, ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeColumn, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  const loading = ref(false);
  const dataSource = ref([]);
  const columns = ref<JVxeColumn[]>([
    { key: 'num', title: 'STT', width: '80px' },
    { key: 'ship_name', title: 'Tên tàu', width: '180px', type: JVxeTypes.input },
    { key: 'call', title: 'Gọi', width: '80px' },
    { key: 'len', title: 'Chiều dài', width: '80px' },
    { key: 'ton', title: 'tấn', width: '120px' },
    { key: 'payer', title: 'Bên thanh toán', width: '120px' },
    { key: 'count', title: 'Số', width: '40px' },
    {
      key: 'company',
      title: 'Công ty',
      minWidth: '180px',
      // Có nhấn để hiển thị thông tin chi tiết không
      // Chỉ có hiệu lực khi ô hiện tại không thể chỉnh sửa
      // Nếu không đặt thì khi nhấp vào chỉ bật popup bảng con, không bật thông tin chi tiết của bảng chính
      showDetails: true,
    },
    { key: 'trend', title: 'Xu hướng', width: '120px' },
  ]);
  const selectedRows = ref([]);
  // Thông tin bảng con
  const subTable = reactive({
    currentRowId: null,
    loading: false,
    pagination: { current: 1, pageSize: 200, pageSizeOptions: ['100', '200'], total: 0 },
    selectedRows: [],
    dataSource: [],
    columns: [
      { key: 'dd_num', title: 'Số thứ tự điều độ', width: '120px' },
      { key: 'tug', title: 'Tàu kéo', width: '180px', type: JVxeTypes.input },
      { key: 'work_start_time', title: 'Thời gian bắt đầu công việc', width: '180px', type: JVxeTypes.input },
      { key: 'work_stop_time', title: 'Thời gian kết thúc công việc', width: '180px', type: JVxeTypes.input },
      { key: 'type', title: 'Phân loại tàu', width: '120px', type: JVxeTypes.input },
      { key: 'port_area', title: 'Khu vực cảng thuộc về', minWidth: '120px', type: JVxeTypes.input },
    ] as JVxeColumn[],
  });

  // formBiểu mẫu col
  const labelCol = reactive({ span: 4 });
  const wrapperCol = reactive({ span: 20 });
  const rules = reactive({
    num: [{ required: true, message: 'Phải nhập số thứ tự' }],
  });

  // Địa chỉ url tìm kiếm
  enum Api {
    getData = '/mock/vxe/getData',
  }

  loadData();

  // Tải dữ liệu
  function loadData() {
    // Đóng gói điều kiện tìm kiếm
    // Gọi API tìm kiếm dữ liệu
    loading.value = true;
    defHttp
      .get({
        url: Api.getData,
        params: {
          pageNo: 1,
          pageSize: 30,
        },
      })
      .then((result) => {
        // Gán dữ liệu truy vấn được cho dataSource
        dataSource.value = result.records;
        // Đặt lại lựa chọn
        selectedRows.value = [];
      })
      .finally(() => {
        // Đây là phương thức sẽ được thực thi dù thành công hay thất bại, tại đây sẽ đóngloading
        loading.value = false;
      });
  }

  // Truy vấn dữ liệu bảng con
  function loadSubData(row) {
    if (row) {
      // Ở đây nhất định phải giới hạn, không được truy vấn lặp lại, nếu không sẽ xuất hiện vòng lặp vô hạn
      if (subTable.currentRowId === row.id) {
        return true;
      }
      subTable.currentRowId = row.id;
      subTable.loading = true;
      defHttp
        .get({
          url: Api.getData,
          params: {
            pageNo: 1,
            pageSize: 30,
            parentId: row.id,
          },
        })
        .then((result) => {
          // Gán dữ liệu truy vấn được cho dataSource
          subTable.dataSource = result.records;
        })
        .finally(() => {
          // Đây là phương thức sẽ được thực thi dù thành công hay thất bại, tại đây sẽ đóngloading
          subTable.loading = false;
        });
      return true;
    } else {
      return false;
    }
  }

  // Đã nhấn nút xác nhận trong thông tin chi tiết
  function handleDetailsConfirm({ row, $table, callback }) {
    console.log('Dữ liệu đã lưu:', row);
    // Kiểm chứng hàng hiện tại
    $table.validate(row).then((errMap) => {
      // Kiểm tra đạt
      if (!errMap) {
        // Kiểm chứng bảng con, nếu cần, có thể thao tác đối tượng dưới đây:
        callback(true);
        loading.value = true;
        setTimeout(() => {
          loading.value = false;
          createMessage.success('Lưu thành công');
        }, 1000);
      } else {
        callback(false);
        createMessage.warn('Kiểm tra thất bại');
      }
    });
  }
</script>

<style scoped></style>
