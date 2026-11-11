<template>
  <JVxeTable
    ref="vTable"
    toolbar
    rowNumber
    rowSelection
    :maxHeight="580"
    :dataSource="dataSource"
    :columns="columns"
    :linkageConfig="linkageConfig"
  />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { JVxeTypes, JVxeColumn, JVxeLinkageConfig } from '/@/components/jeecg/JVxeTable/types';

  // Cấu hình liên động
  const linkageConfig = ref<JVxeLinkageConfig[]>([
    { requestData: requestMockData, key: 's1' },
    // Có thể cấu hình nhiều liên động
    { requestData: requestMenu, key: 'menu1' },
  ]);

  const columns = ref<JVxeColumn[]>([
    {
      title: 'Giới tính',
      key: 'sex',
      type: JVxeTypes.select,
      dictCode: 'sex',
      width: '180px',
      placeholder: 'Vui lòng chọn${title}',
    },
    {
      title: 'Tỉnh/Thành phố trực thuộc trung ương/Khu tự trị',
      key: 's1',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Vui lòng chọn${title}',
      // Trường liên động (tức là trường của cấp tiếp theo)
      linkageKey: 's2',
    },
    {
      title: 'Thành phố',
      key: 's2',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Vui lòng chọn${title}',
      // Trường liên động (tức là trường của cấp tiếp theo)
      linkageKey: 's3',
    },
    {
      title: 'Quận/huyện/Quận',
      key: 's3',
      type: JVxeTypes.select,
      width: '180px',
      options: [],
      placeholder: 'Vui lòng chọn${title}',
    },
    {
      title: 'Menu cấp 1',
      key: 'menu1',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Vui lòng chọn${title}',
      // Trường liên động (tức là trường của cấp tiếp theo)
      linkageKey: 'menu2',
    },
    {
      title: 'Menu cấp hai',
      key: 'menu2',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Vui lòng chọn${title}',
      // Trường liên động (tức là trường của cấp tiếp theo)
      linkageKey: 'menu3',
    },
    {
      title: 'Menu cấp 3',
      key: 'menu3',
      type: JVxeTypes.select,
      width: '180px',
      placeholder: 'Vui lòng chọn${title}',
    },
  ]);

  const dataSource = ref([
    { sex: '1', s1: '110000', s2: '110100', s3: '110101' },
    { sex: '2', s1: '130000', s2: '130300', s3: '130303' },
  ]);

  // Dữ liệu mô phỏng
  const mockData = [
    { text: 'Bắc Kinh', value: '110000', parent: '' },
    { text: 'Thiên Tân', value: '120000', parent: '' },
    { text: 'Hà Bắc', value: '130000', parent: '' },
    { text: 'Thượng Hải', value: '310000', parent: '' },

    { text: 'Bắc Kinh', value: '110100', parent: '110000' },
    { text: 'Thành phố Thiên Tân', value: '120100', parent: '120000' },
    { text: 'Thành phố Thạch Gia Trang', value: '130100', parent: '130000' },
    { text: 'Đường Sơn', value: '130200', parent: '130000' },
    { text: 'Thành phố Tần Hoàng Đảo', value: '130300', parent: '130000' },
    { text: 'Thượng Hải', value: '310100', parent: '310000' },

    { text: 'Quận Đông Thành', value: '110101', parent: '110100' },
    { text: 'Quận Tây Thành', value: '110102', parent: '110100' },
    { text: 'Quận Triều Dương', value: '110105', parent: '110100' },
    { text: 'Quận Hòa Bình', value: '120101', parent: '120100' },
    { text: 'Quận Hà Đông', value: '120102', parent: '120100' },
    { text: 'Quận Hà Tây', value: '120103', parent: '120100' },
    { text: 'Quận Hoàng Phố', value: '310101', parent: '310100' },
    { text: 'Quận Từ Hối', value: '310104', parent: '310100' },
    { text: 'Quận Trường Ninh', value: '310105', parent: '310100' },
    { text: 'Quận Trường An', value: '130102', parent: '130100' },
    { text: 'Quận Kiều Tây', value: '130104', parent: '130100' },
    { text: 'Quận Tân Hoa', value: '130105', parent: '130100' },
    { text: 'Quận Lộ Nam', value: '130202', parent: '130200' },
    { text: 'Quận Lộ Bắc', value: '130203', parent: '130200' },
    { text: 'Quận Cổ Dã', value: '130204', parent: '130200' },
    { text: 'Quận Hải Cảng', value: '130302', parent: '130300' },
    { text: 'Quận Sơn Hải Quan', value: '130303', parent: '130300' },
    { text: 'Quận Bắc Đới Hà', value: '130304', parent: '130300' },
  ];

  /** Mô phỏng truy vấn dữ liệu từ backend */
  function requestMockData(parent) {
    return new Promise((resolve) => {
      let data = mockData.filter((i) => i.parent === parent);
      setTimeout(() => resolve(data), 500);
    });
  }

  /** Tìm kiếm dữ liệu thực từ backend */
  async function requestMenu(parent) {
    let result;
    // Nếu parent trống, thì truy vấn menu cấp một
    if (parent === '') {
      result = await defHttp.get({
        url: '/sys/permission/getSystemMenuList',
        params: {},
      });
    } else {
      result = await defHttp.get({
        url: '/sys/permission/getSystemSubmenu',
        params: { parentId: parent },
      });
    }
    // Dữ liệu trả về bắt buộc phải chứa trường value và text
    return result.map((item) => ({ value: item.id, text: item.name }));
  }
</script>
