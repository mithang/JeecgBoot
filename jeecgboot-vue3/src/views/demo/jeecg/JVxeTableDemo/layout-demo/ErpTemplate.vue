<template>
  <a-card :bordered="false">
    <JVxeTable
      toolbar
      :toolbarConfig="toolbarConfig"
      rowNumber
      rowSelection
      rowSelectionType="radio"
      clickSelectRow
      highlightCurrentRow
      :height="tableHeight"
      :loading="table1.loading"
      :columns="table1.columns"
      :dataSource="table1.dataSource"
      :pagination="table1.pagination"
      :expandConfig="expandConfig"
      style="margin-bottom: 8px"
      @pageChange="handleTable1PageChange"
      @selectRowChange="handleTable1SelectRowChange"
    ></JVxeTable>

    <a-tabs v-show="subTabs.show" :class="{ 'sub-tabs': true, 'un-expand': !subTabs.expand }">
      <a-tab-pane tab="Bảng con1" key="1">
        <JVxeTable
          toolbar
          row-number
          row-selection
          height="auto"
          :maxHeight="350"
          :loading="table2.loading"
          :columns="table2.columns"
          :dataSource="table2.dataSource"
          :pagination="table2.pagination"
          @pageChange="handleTable2PageChange"
          @selectRowChange="handleTable2SelectRowChange"
        />
      </a-tab-pane>
      <a-tab-pane tab="Bảng con2" key="2">
        <h1>Đây là bảng con2</h1>
        <h1>Đây là bảng con2</h1>
        <h1>Đây là bảng con2</h1>
        <h1>Đây là bảng con2</h1>
        <h1>Đây là bảng con2</h1>
        <h1>Đây là bảng con2</h1>
      </a-tab-pane>
    </a-tabs>
  </a-card>
</template>

<script>
  import { h } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { Button, Checkbox } from 'ant-design-vue';
  import { UpOutlined, DownOutlined } from '@ant-design/icons-vue';

  export default {
    name: 'ErpTemplate',
    data() {
      return {
        toolbarConfig: {
          // prefix Tiền tố; suffix Hậu tố
          slot: ['prefix', 'suffix'],
          // add Nút thêm mới; remove là nút xóa; clearSelection là nút xóa lựa chọn
          btn: ['add', 'remove', 'clearSelection'],
        },

        expandConfig: {
          // Có phải chỉ được mở rộng một hàng cùng lúc không
          accordion: true,
        },

        // Bảng con tabs
        subTabs: {
          show: false,
          // Có mở rộng hay không
          expand: true,
          // Có tự động mở rộng không
          autoExpand: true,
        },

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
            showTotal: (total, range) => {
              // Đây là cú pháp jsx
              let text = h('span', `${range[0]}-${range[1]} Tổng cộng ${total} mục`);
              // Kiểm tra xem bảng con có hiển thị hay không, nếu hiển thị thì render nút mở rộng/thu gọn
              if (this.subTabs.show) {
                let expand = h('span', {}, [
                  h(
                    Button,
                    {
                      type: 'link',
                      onClick: this.handleToggleTabs,
                    },
                    () => [this.subTabs.expand ? h(UpOutlined) : h(DownOutlined), h('span', {}, this.subTabs.expand ? 'Thu gọn' : 'Mở rộng')]
                  ),
                  h(
                    Checkbox,
                    {
                      // h cách viết không được hỗ trợ v-model , Vì vậy cần gán giá trị thủ công
                      checked: this.subTabs.autoExpand,
                      'onUpdate:checked': (checked) => (this.subTabs.autoExpand = checked),
                    },
                    () => 'Tự động mở rộng'
                  ),
                ]);
                // Trả về nhiều DOM bằng mảng
                return [expand, text];
              } else {
                // Trả về trực tiếp mộtdom
                return text;
              }
            },
          },
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
            { key: 'call', title: 'Gọi', width: '990px', type: JVxeTypes.input },
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
          currentRowId: null,
          loading: false,
          pagination: { current: 1, pageSize: 10, pageSizeOptions: ['5', '10', '20', '30'], total: 0 },
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
        currentSubRow: null,
        // Địa chỉ url tìm kiếm
        url: {
          getData: '/mock/vxe/getData',
        },
      };
    },
    computed: {
      tableHeight() {
        let { show, expand } = this.subTabs;
        return show ? (expand ? 350 : 482) : 482;
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

      // Truy vấn dữ liệu bảng con
      loadSubData(row) {
        if (row) {
          // Ở đây nhất định phải giới hạn, không được truy vấn lặp lại, nếu không sẽ xuất hiện vòng lặp vô hạn
          if (this.table2.currentRowId === row.id) {
            return true;
          }
          this.table2.currentRowId = row.id;
          this.loadTable2Data();
          return true;
        } else {
          return false;
        }
      },
      // Truy vấn dữ liệu bảng con
      loadTable2Data() {
        let table2 = this.table2;
        let formData = {
          parentId: table2.currentRowId,
          pageNo: this.table2.pagination.current,
          pageSize: this.table2.pagination.pageSize,
        };
        table2.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            // Gán dữ liệu truy vấn được cho dataSource
            table2.selectedRows = [];
            table2.dataSource = result.records;
            table2.pagination.total = result.total;
          })
          .finally(() => {
            // Đây là phương thức sẽ được thực thi dù thành công hay thất bại, tại đây sẽ đóngloading
            table2.loading = false;
          });
      },

      // table1[Bảng chính] Sự kiện kích hoạt khi dòng được chọn thay đổi
      handleTable1SelectRowChange(event) {
        this.table1.selectedRows = event.selectedRows;
        this.subTabs.show = true;
        if (this.subTabs.autoExpand) {
          this.subTabs.expand = true;
        }
        this.loadSubData(event.selectedRows[0]);
      },
      // table2[Bảng con] Sự kiện kích hoạt khi dòng được chọn thay đổi
      handleTable2SelectRowChange(event) {
        this.table2.selectedRows = event.selectedRows;
      },

      handleTable1PageChange(event) {
        // Gán lại giá trị
        this.table1.pagination.current = event.current;
        this.table1.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable1Data();
      },
      // Sự kiện được kích hoạt khi tham số phân trang của table2 [Bảng con] thay đổi
      handleTable2PageChange(event) {
        // Gán lại giá trị
        this.table2.pagination.current = event.current;
        this.table2.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadTable2Data();
      },

      // Mở rộng hoặc thu gọn bảng contabs
      handleToggleTabs() {
        this.subTabs.expand = !this.subTabs.expand;
      },
    },
  };
</script>

<style lang="less" scoped>
  .sub-tabs {
    &.un-expand {
      :deep(.ant-tabs-content) {
        height: 0 !important;
      }

      :deep(.ant-tabs-nav) {
        border-color: transparent !important;
      }

      :deep(.ant-tabs-ink-bar) {
        background-color: transparent !important;
      }

      :deep(.ant-tabs-tab) {
        display: none !important;
      }
    }
  }
</style>
