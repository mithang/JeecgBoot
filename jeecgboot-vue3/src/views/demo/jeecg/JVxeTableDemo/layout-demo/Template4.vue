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
          :height="340"
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
          :height="350"
          :loading="table2.loading"
          :columns="table2.columns"
          :dataSource="table2.dataSource"
          :pagination="table2.pagination"
          @pageChange="handleTable2PageChange"
        />
      </a-col>
      <!-- Dữ liệu được chọn từ cấp cha bên trái hiển thị tại đây -->
      <a-col :span="12">
        <!-- Cha trên phải -->
        <JVxeTable
          rowNumber
          rowSelection
          clickSelectRow
          highlightCurrentRow
          :radioConfig="{ highlight: false }"
          :checkboxConfig="{ highlight: false }"
          :height="340"
          :columns="table1.columns"
          :dataSource="table1.selectedRows"
          style="margin: 52px 0 8px"
          @selectRowChange="handleTable3SelectRowChange"
        />
        <!-- Con dưới phải -->
        <JVxeTable
          toolbar
          rowNumber
          rowSelection
          clickSelectRow
          :height="350"
          :loading="table4.loading"
          :columns="table4.columns"
          :dataSource="table4.dataSource"
          :pagination="table4.pagination"
          style="margin: 48px 0 0"
        />
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';

  export default {
    name: 'Template4',
    data() {
      return {
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
            {
              key: 'company',
              title: 'Công ty',
              // Chiều rộng tối thiểu, khác với chiều rộng ở chỗ đây không phải là chiều rộng cố định, nếu bảng còn không gian thừa, sẽ chia đều cho các cột đã đặt minWidth
              // Nếu muốn làm cột chiếm đầy bảng có thể viết như vậy
              minWidth: '180px',
              type: JVxeTypes.input,
            },
            { key: 'trend', title: 'Xu hướng', width: '120px', type: JVxeTypes.input },
          ],
        },
        // Thông tin cấu hình của bảng cấp con (cấu hình hoàn toàn giống bảng chính nên không viết chú thích thừa nữa)
        table2: {
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
            { key: 'port_area', title: 'Khu vực cảng thuộc về', width: '120px', type: JVxeTypes.input },
          ],
        },
        table3: {
          // Hàng được chọn cuối cùng
          lastRow: null,
          // Hàng đã chọn
          selectedRows: [],
        },
        table4: {
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
      // Lắng nghe dữ liệu được chọn ở table1 phía trên bên trái [Bảng chính] đã thay đổi
      ['table1.lastRow']() {
        this.loadTable2Data();
      },
      // Lắng nghe dữ liệu được chọn ở table3 phía trên bên phải [Bảng chính] đã thay đổi
      ['table3.lastRow']() {
        this.loadTable4Data();
      },
    },
    created() {
      this.loadTable1Data();
    },
    methods: {
      // Tải dữ liệu [Bảng chính] phía trên bên trái của table1
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
            // Đặt lại lựa chọn
            this.table1.selectedRows = [];
          })
          .finally(() => {
            // Đây là phương thức sẽ được thực thi dù thành công hay thất bại, tại đây sẽ đóngloading
            this.table1.loading = false;
          });
      },

      // Sự kiện được kích hoạt khi tham số phân trang của [Bảng chính] ở góc trên bên trái của table1 thay đổi
      handleTable1PageChange(event) {
        // Gán lại giá trị
        this.table1.pagination.current = event.current;
        this.table1.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable1Data();
      },

      // table1Sự kiện kích hoạt khi dòng được chọn thay đổi ở [Bảng chính] phía trên bên trái
      handleTable1SelectRowChange(event) {
        this.handleTableSelectRowChange(this.table1, event);
      },

      // Tải dữ liệu [Bảng con] ở phía dưới bên trái của table2, truy vấn theo id của bảng chính
      loadTable2Data() {
        // Nếu bảng chính chưa được chọn thì không truy vấn
        let selectedRows = this.table1.selectedRows;
        if (!selectedRows || selectedRows.length === 0) {
          this.table2.pagination.total = 0;
          this.table2.dataSource = [];
          this.table2.selectedRows = [];
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
            this.table2.selectedRows = [];
          })
          .finally(() => {
            this.table2.loading = false;
          });
      },

      // Sự kiện được kích hoạt khi tham số phân trang của [Bảng con] ở góc dưới bên trái của table2 thay đổi
      handleTable2PageChange(event) {
        // Gán lại giá trị
        this.table2.pagination.current = event.current;
        this.table2.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable2Data();
      },

      // table3Sự kiện kích hoạt khi dòng được chọn thay đổi ở [Bảng chính] phía trên bên phải
      handleTable3SelectRowChange(event) {
        this.handleTableSelectRowChange(this.table3, event);
      },

      // Tải dữ liệu [Bảng con] ở phía dưới bên phải của table4, truy vấn theo id của bảng chính
      loadTable4Data() {
        let parentIds = [];
        // Nếu bảng chính chưa được chọn thì không truy vấn
        let selectedRows = this.table3.selectedRows;
        if (!selectedRows || selectedRows.length === 0) {
          this.table4.pagination.total = 0;
          this.table4.dataSource = [];
          this.table4.selectedRows = [];
          return;
        } else if (this.table3.lastRow == null) {
          this.table3.lastRow = selectedRows[selectedRows.length - 1];
        }
        let formData = {
          parentId: this.table3.lastRow.id,
          pageNo: this.table4.pagination.current,
          pageSize: this.table4.pagination.pageSize,
        };
        this.table4.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            this.table4.pagination.total = result.total;
            this.table4.dataSource = result.records;
            this.table4.selectedRows = [];
          })
          .finally(() => {
            this.table4.loading = false;
          });
      },

      // Sự kiện được kích hoạt khi tham số phân trang của [Bảng con] ở góc dưới bên phải của table4 thay đổi
      handleTable4PageChange(event) {
        // Gán lại giá trị
        this.table4.pagination.current = event.current;
        this.table4.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable4Data();
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
