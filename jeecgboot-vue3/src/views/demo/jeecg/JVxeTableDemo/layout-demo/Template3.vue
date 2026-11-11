<template>
  <a-card :bordered="false">
    <a-row :gutter="8">
      <a-col :span="12">
        <!-- Cha trên trái -->
        <JVxeTable
          toolbar
          rowNumber
          rowSelection
          clickSelectRow
          highlightCurrentRow
          :radioConfig="{ highlight: false }"
          :checkboxConfig="{ highlight: false }"
          :height="357"
          :loading="table1.loading"
          :columns="table1.columns"
          :dataSource="table1.dataSource"
          :pagination="table1.pagination"
          style="margin-bottom: 8px"
          @pageChange="handleTable1PageChange"
          @selectRowChange="handleTable1SelectRowChange"
        />

        <!-- Con dưới trái -->
        <JVxeTable
          toolbar
          rowNumber
          rowSelection
          clickSelectRow
          :height="356"
          :loading="table2.loading"
          :columns="table2.columns"
          :dataSource="table2.dataSource"
          :pagination="table2.pagination"
          @pageChange="handleTable2PageChange"
        />
      </a-col>
      <!-- Dữ liệu được chọn từ cấp cha bên trái hiển thị tại đây -->
      <a-col :span="12">
        <JVxeTable rowNumber :height="812" :columns="table1.columns" :dataSource="table1.selectedRows" style="margin-top: 52px" />
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';

  // [Nhiều mẫu bố cục] Phía trên bên trái là bảng chính, phía dưới là bảng con, phía phải là dữ liệu đã chọn
  export default {
    name: 'Template3',
    components: {},
    data() {
      return {
        // Thông tin cấu hình bảng chính
        table1: {
          // Có đang tải không
          loading: false,
          // Tham số bộ phân trang
          pagination: {
            // Số trang hiện tại
            current: 1,
            // Số bản ghi mỗi trang
            pageSize: 200,
            // Số mục có thể chuyển đổi
            pageSizeOptions: ['10', '20', '30', '100', '200'],
            // Tổng số dữ liệu (hiện chưa biết tổng số thực tế, nên tạm điền 0, sau khi tra ra ở backend sẽ gán lại)
            total: 0,
          },
          // Hàng được chọn cuối cùng
          lastRow: null,
          // Hàng đã chọn
          selectedRows: [],
          // Nguồn dữ liệu, điều khiển dữ liệu của bảng
          dataSource: [],
          // Cấu hình cột, kiểm soát các cột hiển thị của bảng
          columns: [
            { key: 'num', title: 'STT', width: '80px' },
            {
              // key của trường, khớp với tên trường của dữ liệu backend
              key: 'ship_name',
              // Tiêu đề cột
              title: 'Tên tàu',
              // Độ rộng cột
              width: '180px',
              // Nếu thêm thuộc tính này, nghĩa là ô hiện tại có thể chỉnh sửa, type chính là loại của biểu mẫu, input là ô nhập đơn giản
              type: JVxeTypes.input,
            },
            { key: 'call', title: 'Gọi', width: '80px', type: JVxeTypes.input },
            { key: 'len', title: 'Chiều dài', width: '80px', type: JVxeTypes.input },
            { key: 'ton', title: 'tấn', width: '120px', type: JVxeTypes.input },
            { key: 'payer', title: 'Bên thanh toán', width: '120px', type: JVxeTypes.input },
            { key: 'count', title: 'Số', width: '40px' },
            { key: 'company', title: 'Công ty', width: '180px', type: JVxeTypes.input },
            { key: 'trend', title: 'Xu hướng', width: '120px', type: JVxeTypes.input },
          ],
        },
        // Thông tin cấu hình của bảng con (cấu hình hoàn toàn giống với bảng chính nên không viết chú thích thừa nữa)
        table2: {
          loading: false,
          pagination: { current: 1, pageSize: 200, pageSizeOptions: ['100', '200'], total: 0 },
          dataSource: [],
          columns: [
            { key: 'dd_num', title: 'Số thứ tự điều độ', width: '120px' },
            { key: 'tug', title: 'Tàu kéo', width: '180px', type: JVxeTypes.input },
            { key: 'work_start_time', title: 'Thời gian bắt đầu công việc', width: '180px', type: JVxeTypes.input },
            { key: 'work_stop_time', title: 'Thời gian kết thúc công việc', width: '180px', type: JVxeTypes.input },
            { key: 'type', title: 'Phân loại tàu', width: '120px', type: JVxeTypes.input },
            { key: 'port_area', title: 'Khu vực cảng thuộc về', width: '120px', type: JVxeTypes.input },
          ],
        },
        // Địa chỉ url tìm kiếm
        url: {
          getData: '/mock/vxe/getData',
        },
      };
    },

    // Trình lắng nghe
    watch: {
      // Lắng nghe dữ liệu được chọn ở table1 [Bảng chính] đã thay đổi
      ['table1.lastRow'](row) {
        this.loadTable2Data();
      },
    },
    created() {
      this.loadTable1Data();
    },
    methods: {
      // Tải dữ liệu (Bảng chính) của table1
      loadTable1Data() {
        // Đóng gói điều kiện tìm kiếm
        let formData = {
          pageNo: this.table1.pagination.current,
          pageSize: this.table1.pagination.pageSize,
        };
        // Gọi API tìm kiếm dữ liệu
        this.table1.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            // total trả về từ truy vấn backend, tổng số lượng dữ liệu
            this.table1.pagination.total = result.total;
            // Gán dữ liệu truy vấn được cho dataSource
            this.table1.dataSource = result.records;
          })
          .finally(() => {
            // Đây là phương thức sẽ được thực thi dù thành công hay thất bại, tại đây sẽ đóngloading
            this.table1.loading = false;
          });
      },
      // Tải dữ liệu của table2 (Bảng con), truy vấn theo id của bảng chính
      loadTable2Data() {
        // Nếu bảng chính chưa được chọn thì không truy vấn
        let selectedRows = this.table1.selectedRows;
        if (!selectedRows || selectedRows.length === 0) {
          this.table2.pagination.total = 0;
          this.table2.dataSource = [];
          return;
        } else if (this.table1.lastRow == null) {
          this.table1.lastRow = selectedRows[selectedRows.length - 1];
        }
        let formData = {
          parentId: this.table1.lastRow.id,
          pageNo: this.table2.pagination.current,
          pageSize: this.table2.pagination.pageSize,
        };
        this.table2.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            this.table2.pagination.total = result.total;
            this.table2.dataSource = result.records;
          })
          .finally(() => {
            this.table2.loading = false;
          });
      },

      // table1[Bảng chính] Sự kiện kích hoạt khi tham số phân trang thay đổi
      handleTable1PageChange(event) {
        // Gán lại giá trị
        this.table1.pagination.current = event.current;
        this.table1.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable1Data();
        // Đặt lại lựa chọn sau khi phân trang
        this.table1.selectedRows = [];
        this.loadTable2Data();
      },

      // table2[Bảng con] Sự kiện kích hoạt khi tham số phân trang thay đổi
      handleTable2PageChange(event) {
        // Gán lại giá trị
        this.table1.pagination.current = event.current;
        this.table1.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable2Data();
      },

      // table1[Bảng chính] Sự kiện kích hoạt khi dòng được chọn thay đổi
      handleTable1SelectRowChange(event) {
        this.handleTableSelectRowChange(this.table1, event);
      },

      /** Phương thức chung: xử lý sự kiện thay đổi lựa chọn của bảng */
      handleTableSelectRowChange(table, event) {
        let { row, action, selectedRows, $table } = event;
        // Lấy mục cuối cùng được chọn
        let lastSelected = selectedRows[selectedRows.length - 1];
        if (action === 'selected') {
          table.lastRow = row;
        } else if (action === 'selected-all') {
          // Bỏ chọn tất cả
          if (selectedRows.length === 0) {
            table.lastRow = null;
          } else if (!table.lastRow) {
            table.lastRow = lastSelected;
          }
        } else if (action === 'unselected' && row === table.lastRow) {
          table.lastRow = lastSelected;
        }
        $table.setCurrentRow(table.lastRow);
        table.selectedRows = selectedRows;
      },
    },
  };
</script>

<style scoped></style>
