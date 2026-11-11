<template>
  <a-card :bordered="false">
    <a-row :gutter="8">
      <a-col :span="6">
        <!-- Thêm vào show-line sau thuộc tính, biểu tượng mở rộng/thu gọn tự động đổi thành +- Kiểu -->
        <a-tree
          class="template-5-tree"
          :tree-data="treeData"
          show-icon
          show-line
          :expandedKeys="treeExpandedKeys"
          :selectedKeys="[pagination.current]"
          @expand="handleTreeExpand"
          @select="handleTreeSelect"
        >
          <!-- Tùy chỉnh biểu tượng nút con -->
          <a-icon slot="myIcon" type="unordered-list" style="color: #0c8fcf" />
        </a-tree>
      </a-col>
      <a-col :span="18">
        <JVxeTable
          rowNumber
          rowSelection
          :height="750"
          :loading="loading"
          :columns="columns"
          :dataSource="dataSource"
          :pagination="pagination"
          @pageChange="handleTablePageChange"
        />
      </a-col>
    </a-row>
  </a-card>
</template>

<script>
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes } from '/@/components/jeecg/JVxeTable/types';

  // [Nhiều mẫu bố cục] Bên trái là cây, bên phải là chỉnh sửa dòng
  export default {
    name: 'Template5',
    data() {
      return {
        // Có đang tải không
        loading: false,
        // Tham số bộ phân trang
        pagination: {
          // Số trang hiện tại
          current: 1,
          // Số bản ghi mỗi trang
          pageSize: 50,
          // Số mục có thể chuyển đổi
          pageSizeOptions: ['50'],
          // Tổng số dữ liệu (hiện chưa biết tổng số thực tế, nên tạm điền 0, sau khi tra ra ở backend sẽ gán lại)
          total: 0,
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
        // Dữ liệu của cây, ở đây mô phỏng dữ liệu cố định có phân trang, tình huống thực tế nên là dữ liệu được backend truy vấn ra
        treeData: [
          // Dữ liệu cấp 1
          {
            title: '1-10trang',
            key: '1-10',
            // Dữ liệu cấp 2
            children: [
              { title: 'Trang 1', key: 1, slots: { icon: 'myIcon' } },
              { title: 'Trang 2', key: 2, slots: { icon: 'myIcon' } },
              {
                title: 'Trang 3',
                key: 3,
                slots: { icon: 'myIcon' },
                // Dữ liệu cấp 3
                children: [
                  { title: 'Trang 333', key: 333, slots: { icon: 'myIcon' } },
                  { title: 'Trang 444', key: 444, slots: { icon: 'myIcon' } },
                  { title: 'Trang 555', key: 555, slots: { icon: 'myIcon' } },
                  // Cấp 4, cấp 5 cứ thế suy ra, chỉ cần thêm thuộc tính children là được
                ],
              },
              { title: 'Trang 4', key: 4, slots: { icon: 'myIcon' } },
              { title: 'Trang 5', key: 5, slots: { icon: 'myIcon' } },
              { title: 'Trang 6', key: 6, slots: { icon: 'myIcon' } },
              { title: 'Trang 7', key: 7, slots: { icon: 'myIcon' } },
              { title: 'Trang 8', key: 8, slots: { icon: 'myIcon' } },
              { title: 'Trang 9', key: 9, slots: { icon: 'myIcon' } },
              { title: 'Trang 10', key: 10, slots: { icon: 'myIcon' } },
            ],
            slots: { icon: 'myIcon' },
          },
          {
            title: '11-20trang',
            key: '11-20',
            children: [
              { title: 'Trang 11', key: 11, slots: { icon: 'myIcon' } },
              { title: 'Trang 12', key: 12, slots: { icon: 'myIcon' } },
              { title: 'Trang 13', key: 13, slots: { icon: 'myIcon' } },
              { title: 'Trang 14', key: 14, slots: { icon: 'myIcon' } },
              { title: 'Trang 15', key: 15, slots: { icon: 'myIcon' } },
              { title: 'Trang 16', key: 16, slots: { icon: 'myIcon' } },
              { title: 'Trang 17', key: 17, slots: { icon: 'myIcon' } },
              { title: 'Trang 18', key: 18, slots: { icon: 'myIcon' } },
              { title: 'Trang 19', key: 19, slots: { icon: 'myIcon' } },
              { title: 'Trang 20', key: 20, slots: { icon: 'myIcon' } },
            ],
            slots: { icon: 'myIcon' },
          },
        ],
        // Cột mở rộng dạng cây, mặc định 1-10
        treeExpandedKeys: ['1-10'],
        // Địa chỉ url tìm kiếm
        url: {
          getData: '/mock/vxe/getData',
        },
      };
    },
    created() {
      this.loadData();
    },
    methods: {
      // Tải dữ liệu chỉnh sửa dòng
      loadData() {
        // Đóng gói điều kiện tìm kiếm
        let formData = {
          pageNo: this.pagination.current,
          pageSize: this.pagination.pageSize,
        };
        // Gọi API tìm kiếm dữ liệu
        this.loading = true;
        defHttp
          .get({
            url: this.url.getData,
            params: formData,
          })
          .then((result) => {
            // total trả về từ truy vấn backend, tổng số lượng dữ liệu
            this.pagination.total = result.total;
            // Gán dữ liệu truy vấn được cho dataSource
            this.dataSource = result.records;
            // Đặt lại lựa chọn
            this.selectedRows = [];
          })
          .finally(() => {
            // Đây là phương thức sẽ được thực thi dù thành công hay thất bại, tại đây sẽ đóngloading
            this.loading = false;
          });
      },

      handleTablePageChange(event) {
        // Gán lại giá trị
        this.pagination.current = event.current;
        this.pagination.pageSize = event.pageSize;
        // Truy vấn dữ liệu
        this.loadData();
        // Xác định cây đã mở rộngkey
        if (event.current <= 10) {
          this.treeExpandedKeys = ['1-10'];
        } else {
          this.treeExpandedKeys = ['11-20'];
        }
      },

      // Sự kiện được kích hoạt khi cây được chọn
      handleTreeSelect(selectedKeys) {
        let key = selectedKeys[0];
        if (typeof key === 'string') {
          // Kiểm soát cây mở rộng thành cột đang được chọn
          this.treeExpandedKeys = selectedKeys;
        } else {
          this.pagination.current = key;
          this.loadData();
        }
      },

      // Sự kiện được kích hoạt khi cây được chọn
      handleTreeExpand(expandedKeys) {
        this.treeExpandedKeys = expandedKeys;
      },
    },
  };
</script>

<style lang="less">
  /** Ẩn biểu tượng nhỏ của tệp */
  .template-5-tree.ant-tree {
    li span.ant-tree-switcher.ant-tree-switcher-noop {
      display: none;
    }
  }
</style>
