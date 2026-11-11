<template>
  <a-card title="Ví dụ làm mới không dấu vết" :bordered="false">
    <div style="margin-bottom: 8px">
      <span>Bật hiệu ứng thay đổi dữ liệu:</span>
      <a-switch v-model:checked="reloadEffect" />
    </div>

    <!--
      [Ý tưởng tổng thể về làm mới không dấu vết]:
      1. Chức năng này phụ thuộc vào chức năng [Lưu tức thì], vui lòng xem ví dụ lưu tức thì trước
      2. Bắt buộc phải có socket-reload thuộc tính, và đặt thành true
      3. Bắt buộc phải có socket-key Thuộc tính, thuộc tính này là định danh duy nhất của bảng hiện tại,
         Hệ thống sẽ tự động cập nhật tất cả socket-key Bảng giống nhau
      4. Lưu cục bộ edit-closed trong sự kiện,
         Sau khi lưu thành công gọi phương thức socketSendUpdateRow, chỉ cần truyền row hiện tại qua là được (xem dòng 102)
    -->
    <JVxeTable
      ref="tableRef"
      rowNumber
      rowSelection
      keepSource
      socketReload
      socketKey="demo-socket-reload"
      :reloadEffect="reloadEffect"
      :height="340"
      :loading="loading"
      :columns="columns"
      :dataSource="dataSource"
      @valueChange="onValueChange"
      @edit-closed="handleEditClosed"
    />
  </a-card>
</template>

<script lang="ts" setup>
  // Ví dụ làm mới không dấu vết
  import { ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeColumn, JVxeTableInstance, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';

  const { createMessage } = useMessage();
  const tableRef = ref<JVxeTableInstance>();
  // Có bật hiệu ứng làm mới lịch không
  const reloadEffect = ref(true);
  const loading = ref(false);
  const dataSource = ref<Recordable[]>([]);
  const columns = ref<JVxeColumn[]>([
    { key: 'num', title: 'STT', width: 80 },
    { key: 'enabled', title: 'Kích hoạt', width: 80, type: JVxeTypes.checkbox },
    { key: 'ship_name', title: 'Tên tàu', width: 180, type: JVxeTypes.input },
    { key: 'call', title: 'Gọi', width: 80, type: JVxeTypes.input },
    { key: 'len', title: 'Chiều dài', width: 80, type: JVxeTypes.input },
    { key: 'ton', title: 'tấn', width: 120, type: JVxeTypes.input },
    { key: 'payer', title: 'Bên thanh toán', width: 120, type: JVxeTypes.input },
    { key: 'count', title: 'Số', width: 40 },
    { key: 'company', title: 'Công ty', minWidth: 180, type: JVxeTypes.input },
    { key: 'trend', title: 'Xu hướng', width: 120, type: JVxeTypes.input },
  ]);

  // Địa chỉ url tìm kiếm
  enum Api {
    getData = '/mock/vxe/getData',
  }

  loadData();

  // Tải dữ liệu
  function loadData() {
    loading.value = true;
    defHttp
      .get({
        url: Api.getData,
        params: { pageNo: 1, pageSize: 200 },
      })
      .then((result) => {
        dataSource.value = result.records;
      })
      .finally(() => {
        loading.value = false;
      });
  }

  /** Sự kiện được kích hoạt khi giá trị ô thay đổi */
  function onValueChange(event) {
    switch (event.type) {
      // Tất cả các component không thể kích hoạt sự kiện editClosed đều cần được định nghĩa tại đây, có thể hoàn thiện tại đây theo nhu cầu nghiệp vụ của bạncase
      case JVxeTypes.radio:
      case JVxeTypes.checkbox:
        doSendUpdateRow(event);
        break;
    }
  }

  // Sự kiện được kích hoạt sau khi chỉnh sửa ô hoàn tất
  function handleEditClosed(event) {
    doSendUpdateRow(event);
  }

  // Gửi yêu cầu thay đổi hàng
  function doSendUpdateRow(event) {
    let { $table, row, column } = event;
    let field = column.property;
    // Kiểm tra xem giá trị ô có bị sửa đổi không
    if ($table.isUpdateByRow(row, field)) {
      // Kiểm chứng hàng hiện tại
      $table.validate(row).then((errMap) => {
        // Kiểm tra đạt
        if (!errMap) {
          // [Mô phỏng lưu] (chỗ này cần thay bằng yêu cầu thật)
          let hideLoading = createMessage.loading(`Đang lưu..."${column.title}"`, 0);
          setTimeout(() => {
            hideLoading();
            createMessage.success(`"${column.title}"Lưu thành công!`);
            // Cập nhật cục bộ ô thành trạng thái đã lưu
            $table.reloadRow(row, null, field);
            // Gửi thông báo cập nhật
            tableRef.value?.socketSendUpdateRow(row);
          }, 555);
        }
      });
    }
  }
</script>

<style scoped></style>
