<template>
  <JVxeTable
    ref="tableRef"
    toolbar
    row-number
    row-selection
    keep-source
    :height="492"
    :loading="loading"
    :dataSource="dataSource"
    :columns="columns"
    :pagination="pagination"
    style="margin-top: 8px"
    @pageChange="handlePageChange"
  >
    <template #toolbarSuffix>
      <a-button @click="handleTableGet">Lấy dữ liệu</a-button>
    </template>
  </JVxeTable>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  // noinspection ES6UnusedImports
  import { Popconfirm } from 'ant-design-vue';
  import { JVxeColumn, JVxeTableInstance, JVxeTypes } from '/@/components/jeecg/JVxeTable/types';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { random } from 'lodash-es';
  import { buildUUID } from '/@/utils/uuid';
  import { uploadUrl } from '/@/api/common/api';

  const tableRef = ref<JVxeTableInstance>();
  const { createMessage } = useMessage();
  const loading = ref(false);
  const columns = ref<JVxeColumn[]>([
    {
      title: 'Dropdown_Tìm kiếm bảng từ điển',
      key: 'select_dict_search',
      type: JVxeTypes.selectDictSearch,
      width: 200,
      async: true, // Tìm kiếm không đồng bộ, mặc định là true
      // Thông tin cấu hình bảng từ điển: Tên bảng cơ sở dữ liệu,Tên trường hiển thị,Tên trường lưu trữ
      dict: 'sys_user,realname,username',
      tipsContent: 'Vui lòng nhập điều kiện tìm kiếm',
    },
    {
      title: 'JPopup',
      key: 'popup',
      type: JVxeTypes.popup,
      width: 180,
      popupCode: 'demo',
      // field: 'name,sex,age',
      // orgFields: 'name,sex,age',
      // destFields: 'popup,popup_sex,popup_age',
      fieldConfig: [
        { source: 'name', target: 'popup' },
        { source: 'sex', target: 'popup_sex' },
        { source: 'age', target: 'popup_age' },
      ],
    },
    {
      title: 'JP-Giới tính',
      key: 'popup_sex',
      type: JVxeTypes.select,
      dictCode: 'sex',
      disabled: true,
      width: 100,
    },
    {
      title: 'JP-Tuổi',
      key: 'popup_age',
      type: JVxeTypes.normal,
      width: 80,
    },
    {
      title: 'Chọn người dùng',
      key: 'userSelect',
      type: JVxeTypes.userSelect,
      width: 240,
    },
    {
      title: 'Chọn phòng ban',
      key: 'departSelect',
      type: JVxeTypes.departSelect,
      width: 240,
    },
    {
      title: 'Chọn một',
      key: 'radio',
      type: JVxeTypes.radio,
      width: 130,
      options: [
        { text: 'Nam', value: '1' },
        { text: 'Nữ', value: '2' },
      ],
      // Cho phép xóa lựa chọn (bấm thêm một lần nữa để hủy chọn)
      allowClear: false,
    },
    {
      title: 'Tải lên',
      key: 'upload',
      type: JVxeTypes.upload,
      width: 180,
      btnText: 'Nhấn để tải lên',
      token: true,
      responseName: 'message',
      action: uploadUrl,
    },
    {
      title: 'Tải lên hình ảnh',
      key: 'image',
      type: JVxeTypes.image,
      width: 180,
      maxCount: 6,
    },
    {
      title: 'Tải lên tệp',
      key: 'file',
      type: JVxeTypes.file,
      width: 180,
      maxCount: 2,
    },
    {
      title: 'Thanh tiến độ',
      key: 'progress',
      type: JVxeTypes.progress,
      minWidth: 320,
    },
  ]);
  const dataSource = ref<any[]>([]);
  const pagination = reactive({
    current: 1,
    pageSize: 10,
    pageSizeOptions: ['10', '20', '30', '100', '200'],
    total: 1000,
  });

  randomPage(pagination.current, pagination.pageSize, true);

  // Sự kiện được kích hoạt khi tham số phân trang thay đổi
  function handlePageChange(event) {
    // Gán lại giá trị
    pagination.current = event.current;
    pagination.pageSize = event.pageSize;
    // Truy vấn dữ liệu
    randomPage(event.current, event.pageSize, true);
  }

  /** Lấy giá trị, bỏ qua kiểm tra biểu mẫu */
  function handleTableGet() {
    const values = tableRef.value!.getTableData();
    console.log('Lấy giá trị:', { values });
    createMessage.success('Lấy giá trị thành công, vui lòng xem kết quả trong console');
  }

  /* Tạo ngẫu nhiên dữ liệu phân trang */
  function randomPage(current, pageSize, $loading = false) {
    if ($loading) {
      loading.value = true;
    }
    let begin = Date.now();
    let values: any[] = [];
    for (let i = 0; i < pageSize; i++) {
      let radio = random(0, 2);
      values.push({
        id: buildUUID(),
        select_dict_search: ['admin', '', 'jeecg'][random(0, 2)],
        progress: random(0, 100),
        radio: radio ? radio.toString() : null,
      });
    }
    dataSource.value = values;
    let end = Date.now();
    let diff = end - begin;
    if ($loading && diff < pageSize) {
      setTimeout(() => (loading.value = false), pageSize - diff);
    }
  }
</script>
