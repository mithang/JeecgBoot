<template>
  <a-card :bordered="false">
    <a-row :gutter="8">
      <!-- Đây là nút cha -->
      <a-col :span="24" style="margin-bottom: 4px">
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
          @pageChange="handleTable1PageChange"
          @selectRowChange="handleTable1SelectRowChange"
        />
      </a-col>
      <!-- Đây là nút con -->
      <a-col :span="12">
        <JVxeTable
          toolbar
          rowNumber
          rowSelection
          clickSelectRow
          highlightCurrentRow
          :radioConfig="{ highlight: false }"
          :checkboxConfig="{ highlight: false }"
          :height="340"
          :loading="table2.loading"
          :columns="table2.columns"
          :dataSource="table2.dataSource"
          :pagination="table2.pagination"
          @pageChange="handleTable2PageChange"
          @selectRowChange="handleTable2SelectRowChange"
        >
        </JVxeTable>
      </a-col>
      <!-- Đây là nút cháu -->
      <a-col :span="12">
        <JVxeTable
          toolbar
          rowNumber
          rowSelection
          :height="340"
          :loading="table3.loading"
          :columns="table3.columns"
          :dataSource="table3.dataSource"
          :pagination="table3.pagination"
          @pageChange="handleTable3PageChange"
        >
        </JVxeTable>
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';

  // [Nhiều mẫu bố cục] Trên là bảng cha, dưới trái là bảng con, dưới phải là bảng cháu
  export default {
    name: 'Template1',
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
              formatter({ cellValue, row, column }) {
                let foo = '';
                if (row.company === '佧伒侾佯有限公司') {
                  foo += '-233';
                }
                return cellValue + foo;
              },
            },
            { key: 'call', title: 'Gọi', width: '80px', type: JVxeTypes.input },
            { key: 'len', title: 'Chiều dài', width: '80px', type: JVxeTypes.inputNumber },
            { key: 'ton', title: 'tấn', width: '120px', type: JVxeTypes.inputNumber },
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
          // Hàng được chọn cuối cùng
          lastRow: null,
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
        // Thông tin cấu hình của bảng cấp cháu (cấu hình hoàn toàn giống bảng chính nên không viết chú thích thừa nữa)
        table3: {
          loading: false,
          pagination: { current: 1, pageSize: 200, pageSizeOptions: ['100', '200'], total: 0 },
          selectedRows: [],
          dataSource: [],
          columns: [
            { key: 'dd_num', title: 'Số thứ tự điều độ', width: '120px' },
            { key: 'tug', title: 'Tàu kéo', width: '120px', type: JVxeTypes.input },
            { key: 'power', title: 'Mã lực', width: '120px', type: JVxeTypes.input },
            { key: 'nature', title: 'Tính chất', width: '120px', type: JVxeTypes.input },
            { key: 'departure_time', title: 'Thời gian tàu khởi hành', width: '180px', type: JVxeTypes.input },
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
      // Lắng nghe dữ liệu được chọn ở table2 [Bảng con] đã thay đổi
      ['table2.lastRow']() {
        this.loadTable3Data();
      },
    },
    created() {
      this.loadTable1Data();
    },
    methods: {
      // Tải dữ liệu [Bảng chính] của table1
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

      // Sự kiện được kích hoạt khi tham số phân trang của table1 [Bảng chính] thay đổi
      handleTable1PageChange(event) {
        // Gán lại giá trị
        this.table1.pagination.current = event.current;
        this.table1.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable1Data();
      },

      // table1[Bảng chính] Sự kiện kích hoạt khi dòng được chọn thay đổi
      handleTable1SelectRowChange(event) {
        this.handleTableSelectRowChange(this.table1, event);
      },

      // Tải dữ liệu của table2 [Bảng con], truy vấn theo id của bảng chính
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

      // table2[Bảng con] Sự kiện kích hoạt khi dòng được chọn thay đổi
      handleTable2SelectRowChange(event) {
        this.handleTableSelectRowChange(this.table2, event);
      },

      // Sự kiện được kích hoạt khi tham số phân trang của table2 [Bảng con] thay đổi
      handleTable2PageChange(event) {
        // Gán lại giá trị
        this.table2.pagination.current = event.current;
        this.table2.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable2Data();
      },

      // Tải dữ liệu của table3 [Bảng cháu], truy vấn theo id của bảng con
      loadTable3Data() {
        // Nếu bảng chính chưa được chọn thì không truy vấn
        let selectedRows = this.table2.selectedRows;
        if (!selectedRows || selectedRows.length === 0) {
          this.table3.pagination.total = 0;
          this.table3.dataSource = [];
          this.table3.selectedRows = [];
          return;
        } else if (this.table2.lastRow == null) {
          this.table2.lastRow = selectedRows[selectedRows.length - 1];
        }
        let formData = {
          parentId: this.table2.lastRow.id,
          pageNo: this.table3.pagination.current,
          pageSize: this.table3.pagination.pageSize,
        };
        this.table3.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            this.table3.pagination.total = result.total;
            this.table3.dataSource = result.records;
          })
          .finally(() => {
            this.table3.loading = false;
          });
      },
      // Sự kiện được kích hoạt khi tham số phân trang của table3 [Bảng cháu] thay đổi
      handleTable3PageChange(event) {
        // Gán lại giá trị
        this.table3.pagination.current = event.current;
        this.table3.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable3Data();
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

<style lang="less"></style>
