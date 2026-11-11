<template>
  <a-card title="Ví dụ lưu tức thì" :bordered="false">
    <!--
      [Ý tưởng tổng thể về lưu tức thì]:
      1. JVxeTable phải thêm vào keep-source Thuộc tính
      2. Lắng nghe edit-closedSự kiện này kích hoạt sau khi chỉnh sửa xong
      3. Trong sự kiện này, xác định dữ liệu có thay đổi không, nếu thay đổi thì gọi API để thực hiện thao tác lưu
    -->
    <JVxeTable
      toolbar
      :toolbarConfig="toolbarConfig"
      rowNumber
      rowSelection
      keepSource
      asyncRemove
      :height="340"
      :loading="loading"
      :columns="columns"
      :dataSource="dataSource"
      :pagination="pagination"
      @save="handleTableSave"
      @removed="handleTableRemove"
      @edit-closed="handleEditClosed"
      @pageChange="handlePageChange"
      @selectRowChange="handleSelectRowChange"
    />
  </a-card>
</template>

<script lang="ts" setup>
  // Ví dụ lưu tức thì
  import { reactive, ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeColumn, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  // Cấu hình nút của thanh công cụ
  const toolbarConfig = reactive({
    // add Nút thêm mới; remove là nút xóa; clearSelection là nút xóa lựa chọn
    btn: ['add', 'save', 'remove', 'clearSelection'],
  });
  // Có đang tải không
  const loading = ref(false);
  // Tham số bộ phân trang
  const pagination = reactive({
    // Số trang hiện tại
    current: 1,
    // Số bản ghi mỗi trang
    pageSize: 200,
    // Số mục có thể chuyển đổi
    pageSizeOptions: ['10', '20', '30', '100', '200'],
    // Tổng số dữ liệu (hiện chưa biết tổng số thực tế, nên tạm điền 0, sau khi tra ra ở backend sẽ gán lại)
    total: 0,
  });
  // Hàng đã chọn
  const selectedRows = ref<Recordable[]>([]);
  // Nguồn dữ liệu, điều khiển dữ liệu của bảng
  const dataSource = ref<Recordable[]>([]);
  // Cấu hình cột, kiểm soát các cột hiển thị của bảng
  const columns = ref<JVxeColumn[]>([
    { key: 'num', title: 'STT', width: 80, type: JVxeTypes.normal },
    {
      // key của trường, khớp với tên trường của dữ liệu backend
      key: 'ship_name',
      // Tiêu đề cột
      title: 'Tên tàu',
      // Độ rộng cột
      width: 180,
      // Nếu thêm thuộc tính này, nghĩa là ô hiện tại có thể chỉnh sửa, type chính là loại của biểu mẫu, input là ô nhập đơn giản
      type: JVxeTypes.input,
    },
    { key: 'call', title: 'Gọi', width: 80, type: JVxeTypes.input },
    { key: 'len', title: 'Chiều dài', width: 80, type: JVxeTypes.input },
    { key: 'ton', title: 'tấn', width: 120, defaultValue: 233, type: JVxeTypes.input },
    { key: 'payer', title: 'Bên thanh toán', width: 120, defaultValue: 'Trương Tam', type: JVxeTypes.input },
    { key: 'count', title: 'Số', width: 40, type: JVxeTypes.normal },
    {
      key: 'company',
      title: 'Công ty',
      // Chiều rộng tối thiểu, khác với chiều rộng ở chỗ đây không phải là chiều rộng cố định, nếu bảng còn không gian thừa, sẽ chia đều cho các cột đã đặt minWidth
      // Nếu muốn làm cột chiếm đầy bảng có thể viết như vậy
      minWidth: 180,
      type: JVxeTypes.input,
    },
    { key: 'trend', title: 'Xu hướng', width: 120, type: JVxeTypes.input },
  ]);

  // Địa chỉ url tìm kiếm
  enum Api {
    getData = '/mock/vxe/getData',
    // Mô phỏng lưu dữ liệu một dòng (lưu ngay lập tức)
    saveRow = '/mock/vxe/immediateSaveRow',
    // Mô phỏng việc lưu dữ liệu của toàn bộ bảng
    saveAll = '/mock/vxe/immediateSaveAll',
  }

  loadData();

  // Tải dữ liệu
  async function loadData() {
    loading.value = true;
    // Gọi API tìm kiếm dữ liệu
    await defHttp
      .get({
        // Địa chỉ yêu cầu
        url: Api.getData,
        // Đóng gói điều kiện tìm kiếm
        params: {
          pageNo: pagination.current,
          pageSize: pagination.pageSize,
        },
      })
      .then((result) => {
        // total trả về từ truy vấn backend, tổng số lượng dữ liệu
        pagination.total = result.total;
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

  // [Lưu toàn bộ] Sự kiện kích hoạt khi bấm nút Lưu
  function handleTableSave({ $table, target }) {
    // Kiểm chứng toàn bộ bảng
    $table.validate().then((errMap) => {
      // Kiểm tra đạt
      if (!errMap) {
        // Lấy tất cả dữ liệu
        let tableData = target.getTableData();
        console.log('Dữ liệu hiện đang lưu là:', tableData);
        // Lấy dữ liệu mới thêm
        let newData = target.getNewData();
        console.log('-- Dữ liệu mới thêm:', newData);
        // Lấy dữ liệu đã xóa
        let deleteData = target.getDeleteData();
        console.log('-- Dữ liệu đã xóa:', deleteData);
        // [Mô phỏng lưu]
        loading.value = true;
        defHttp
          .post({
            url: Api.saveAll,
            params: tableData,
          })
          .then(() => {
            createMessage.success(`Lưu thành công!`);
          })
          .finally(() => {
            loading.value = false;
          });
      }
    });
  }

  // Kích hoạt sự kiện xóa ô
  function handleTableRemove(event) {
    //  event.deleteRows Truyền cho backend để xóa (Lưu ý: ở đây sẽ không truyền dữ liệu mới thêm theo logic frontend, vì không cần yêu cầu backend xóa)
    console.log('Dữ liệu chờ xóa: ', event.deleteRows);
    // Cũng có thể chỉ truyền ID, vì có thể xóa theo ID
    let deleteIds = event.deleteRows.map((row) => row.id);
    console.log('Dữ liệu chờ xóaids: ', deleteIds);

    // Mô phỏng yêu cầu xóa ở backend
    loading.value = true;
    window.setTimeout(() => {
      loading.value = false;
      createMessage.success('Xóa thành công');
      // Giả sử backend trả về xóa thành công, bắt buộc phải gọi confirmRemove() phương thức, mới thực sự xóa khỏi bảng (đồng thời sẽ xóa dữ liệu thêm mới theo logic đã chọn)
      event.confirmRemove();
    }, 1000);
  }

  // Sự kiện được kích hoạt sau khi chỉnh sửa ô hoàn tất
  function handleEditClosed(event) {
    let { $table, row, column } = event;
    let field = column.property;
    // Kiểm tra xem giá trị ô có bị sửa đổi không
    if ($table.isUpdateByRow(row, field)) {
      // Kiểm chứng hàng hiện tại
      $table.validate(row).then((errMap) => {
        // Kiểm tra đạt
        if (!errMap) {
          // [Mô phỏng lưu]
          let hideLoading = createMessage.loading(`Đang lưu..."${column.title}"`, 0);
          console.log('Lưu dữ liệu ngay lập tức:', row);
          defHttp
            .put({
              url: Api.saveRow,
              params: row,
            })
            .then((res) => {
              createMessage.success(`"${column.title}"Lưu thành công!`);
              // Cập nhật cục bộ ô thành trạng thái đã lưu
              $table.reloadRow(row, null, field);
            })
            .finally(() => {
              hideLoading();
            });
        }
      });
    }
  }

  // Sự kiện được kích hoạt khi tham số phân trang thay đổi
  function handlePageChange(event) {
    // Gán lại giá trị
    pagination.current = event.current;
    pagination.pageSize = event.pageSize;
    // Truy vấn dữ liệu
    loadData();
  }

  // Sự kiện được kích hoạt khi dòng đã chọn thay đổi
  function handleSelectRowChange(event) {
    selectedRows.value = event.selectedRows;
  }
</script>

<style scoped></style>
