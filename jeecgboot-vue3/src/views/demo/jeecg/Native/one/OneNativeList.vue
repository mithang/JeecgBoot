<template>
  <a-card :bordered="false">
    <!-- Khu vực nút thao tác -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" preIcon="ant-design:plus">Thêm mới</a-button>
      <!--      <a-button type="primary" preIcon="ant-design:download" @click="handleExportExcel('Danh sách gốc đơn bảng')">Xuất</a-button>-->
      <!--      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="handleImportExcel">Import</j-upload-button>-->
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchDel">
              <Icon icon="ant-design:delete-outlined"></Icon>
              Xóa
            </a-menu-item>
          </a-menu>
        </template>
        <a-button
          >Thao tác hàng loạt
          <Icon icon="mdi:chevron-down"></Icon>
        </a-button>
      </a-dropdown>
    </div>

    <!-- tableKhu vực-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> Đã chọn <a style="font-weight: 600">{{ selectedRowKeys.length }}</a
        >Mục
        <a style="margin-left: 24px" @click="onClearSelected">Xóa sạch</a>
      </div>

      <a-table
        ref="table"
        size="middle"
        :scroll="{ x: true }"
        bordered
        rowKey="id"
        class="j-table-force-nowrap"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{ selectedRowKeys: selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex==='tupian'">
            <span v-if="!text" style="font-size: 12px; font-style: italic">Không có hình ảnh</span>
            <img v-else :src="getImgView(text)" :preview="record.id" alt="" class="anty-img-wrap" />
          </template>
          <template v-else-if="column.dataIndex==='wenjian'">
            <span v-if="!text" style="font-size: 12px; font-style: italic">Không có tệp</span>
            <a-button v-else :ghost="true" type="primary" preIcon="ant-design:download" size="small" @click="downloadFile(text)"> Tải xuống </a-button>
          </template>
          <template v-else-if="column.dataIndex==='action'">
            <a @click="handleEdit(record)">Chỉnh sửa</a>
            <a-divider type="vertical" />
            <a-dropdown>
              <a class="ant-dropdown-link">Thêm <Icon icon="mdi-light:chevron-down"></Icon></a>
              <template #overlay>
                <a-menu class="antd-more">
                  <a-menu-item>
                    <a @click="handleDetail(record)">Chi tiết</a>
                  </a-menu-item>
                  <a-menu-item>
                    <Popconfirm title="Bạn có chắc chắn muốn xóa không?" @confirm="() => handleDelete(record.id)" placement="left">
                      <a>Xóa</a>
                    </Popconfirm>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
          <!-- <template v-else-if="column.dataIndex==='htmlSlot'">
            <div v-html="text"></div>
          </template>
          <template v-else-if="column.dataIndex==='pcaSlot'">
            <div>{{ getAreaTextByCode(text) }}</div>
          </template> -->
        </template>
      </a-table>
    </div>
    <OneNativeModal ref="oneProtogenesisModal" @ok="handleSuccess"></OneNativeModal>
  </a-card>
</template>

<script lang="ts" setup>
  import '../less/TableExpand.less';
  import { onMounted, ref, reactive } from 'vue';
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { filterMultiDictText } from '/@/utils/dict/JDictSelectUtil.js';
  import { getAreaTextByCode } from '/@/components/Form/src/utils/Area';
  import OneNativeModal from './components/OneNativeModal.vue';
  import { Modal, Popconfirm } from 'ant-design-vue';
  import { JSelectUserByDept, JDictSelectTag, JSelectDept, JSearchSelect } from '/@/components/Form';
  import Icon from '/@/components/Icon/index';
  import { filterObj, getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import { loadCategoryData } from '/@/api/common/api';
  import { getToken } from '/@/utils/auth';
  import { useMethods } from '/@/hooks/system/useMethods';
  import { downloadFile } from '/@/utils/common/renderUtils';
  import { initDictOptions } from '/@/utils/dict';

  const { handleExportXls, handleImportXls } = useMethods();
  const modalVisible = ref<boolean>(false);
  const queryParam = ref<any>({});
  const loading = ref<boolean>(false);
  const dictOptions = ref<any>([]);
  const oneProtogenesisModal = ref();
  const tokenHeader = { 'X-Access-Token': getToken() };
  //Tiêu đề bảng
  const columns = ref<any>([
    {
      title: 'Văn bản',
      align: 'center',
      dataIndex: 'name',
    },
    {
      title: 'Danh sách thả xuống từ điển',
      align: 'center',
      dataIndex: 'xiala',
      customRender: ({ text }) => (text ? filterMultiDictText(dictOptions.value['xiala'], text) : ''),
    },
    {
      title: 'Chọn một từ điển',
      align: 'center',
      dataIndex: 'danxuan',
      customRender: ({ text }) => (text ? filterMultiDictText(dictOptions.value['danxuan'], text) : ''),
    },
    {
      title: 'Chọn nhiều từ điển',
      align: 'center',
      dataIndex: 'duoxuan',
      customRender: ({ text }) => (text ? filterMultiDictText(dictOptions.value['duoxuan'], text) : ''),
    },
    {
      title: 'Công tắc',
      align: 'center',
      dataIndex: 'kaiguan',
      customRender: ({ text }) => (text ? filterMultiDictText(dictOptions.value['kaiguan'], text) : ''),
    },
    {
      title: 'Ngày',
      align: 'center',
      dataIndex: 'riqi',
      customRender: function ({ text }) {
        return !text ? '' : text.length > 10 ? text.substr(0, 10) : text;
      },
    },
    {
      title: 'Năm tháng ngày giờ phút giây',
      align: 'center',
      dataIndex: 'nyrsfm',
    },
    {
      title: 'Thời gian',
      align: 'center',
      dataIndex: 'shijian',
    },
    {
      title: 'Tệp',
      align: 'center',
      dataIndex: 'wenjian',
    },
    {
      title: 'Hình ảnh',
      align: 'center',
      dataIndex: 'tupian',
    },
    {
      title: 'Thao tác',
      dataIndex: 'action',
      align: 'center',
      fixed: 'right',
      width: 147,
    },
  ]);

  const Api = reactive<any>({
    list: '/test/jeecgDemo/oneNative/list',
    delete: '/test/jeecgDemo/oneNative/delete',
    exportXls: '/test/jeecgDemo/oneNative/exportXls',
    importExcel: 'test/jeecgDemo/oneNative/importExcel',
  });

  const dataSource = ref<any>([]);
  const toggleSearchStatus = ref<boolean>(false);
  const ipagination = ref<any>({
    current: 1,
    pageSize: 10,
    pageSizeOptions: ['10', '20', '30'],
    showTotal: (total, range) => {
      return range[0] + '-' + range[1] + ' Tổng cộng' + total + 'mục';
    },
    showQuickJumper: true,
    showSizeChanger: true,
    total: 0,
  });

  const selectedRowKeys = ref<any>([]);
  const selectionRows = ref<any>([]);
  const iSorter = ref<any>({ column: 'createTime', order: 'desc' });
  const iFilters = ref<any>({});
  const { createMessage } = useMessage();

  /**
   * Sự kiện chọn checkbox
   * @param rowKeys
   * @param rows
   */
  function onSelectChange(rowKeys, rows) {
    selectedRowKeys.value = rowKeys;
    selectionRows.value = rows;
  }

  /**
   * Sự kiện thay đổi bảng
   */
  function handleTableChange({ pagination, filters, sorter }) {
    ipagination.value = pagination;
    iSorter.value = sorter;
    iFilters.value = { ...filters };
  }

  /**
   * Thêm mới
   */
  function handleAdd() {
    oneProtogenesisModal.value.disableSubmit = false;
    oneProtogenesisModal.value.add();
  }

  /**
   * Xóa hàng đã chọn
   */
  function onClearSelected() {
    selectedRowKeys.value = [];
    selectionRows.value = [];
  }

  /**
   * Xóa hàng loạt
   */
  function batchDel() {
    Modal.confirm({
      title: 'Xác nhận xóa',
      content: 'Có xóa dữ liệu đã chọn không',
      okText: 'Xác nhận',
      cancelText: 'Hủy',
      onOk: () => {
        defHttp.delete({ url: Api.delete, data: { ids: selectedRowKeys.value } }, { joinParamsToUrl: true }).then(() => {
          handleSuccess();
        });
      },
    });
  }

  /**
   * Xuấtexcel
   */
  function handleExportExcel(title) {
    let paramsForm = getQueryParams();
    if (selectedRowKeys.value && selectedRowKeys.value.length > 0) {
      paramsForm['selections'] = selectedRowKeys.join(',');
    }
    handleExportXls(title, Api.exportXls, filterObj(paramsForm));
  }

  /**
   * Importexcel
   */
  function handleImportExcel(file) {
    handleImportXls(file, Api.importExcel, '').then(() => {
      handleSuccess();
    });
  }

  /**
   * Lấy tham số truy vấn
   */
  function getQueryParams() {
    let params = Object.assign(queryParam.value, iSorter.value, iFilters.value);
    params.field = getQueryField();
    params.pageNo = ipagination.value.current;
    params.pageSize = ipagination.value.pageSize;
    return filterObj(params);
  }

  /**
   * Kiểm soát quyền trường
   */
  function getQueryField() {
    let str = 'id,';
    columns.value.forEach(function (value) {
      str += ',' + value.dataIndex;
    });
    return str;
  }

  /**
   * Dữ liệu khởi tạo
   */
  function loadData(arg?) {
    if (arg === 1) {
      ipagination.value.current = 1;
    }
    loading.value = true;
    let params = getQueryParams();
    defHttp
      .get({ url: Api.list, params }, { isTransformResponse: false })
      .then((res) => {
        if (res.success) {
          dataSource.value = res.result.records;
          if (res.result && res.result.total) {
            ipagination.value.total = res.result.total;
          } else {
            ipagination.value.total = 0;
          }
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        loading.value = false;
      });
  }

  //Truy vấn
  function searchQuery() {
    loadData(1);
    selectedRowKeys.value = [];
    selectionRows.value = [];
  }

  /**
   * Mở rộng/thu gọn vùng tìm kiếm
   */
  function handleToggleSearch() {
    toggleSearchStatus.value = !toggleSearchStatus.value;
  }

  /**
   * Nút đặt lại
   */
  function searchReset() {
    queryParam.value = {};
    loadData(1);
  }

  /**
   * Lấy hình ảnh xem trước
   */
  function getImgView(text) {
    if (text && text.indexOf(',') > 0) {
      text = text.substring(0, text.indexOf(','));
    }
    return getFileAccessHttpUrl(text);
  }

  /**
   * Chỉnh sửa
   * @param record
   */
  function handleEdit(record) {
    oneProtogenesisModal.value.disableSubmit = false;
    oneProtogenesisModal.value.edit(record);
  }

  /**
   * Chi tiết
   * @param record
   */
  function handleDetail(record) {
    oneProtogenesisModal.value.disableSubmit = true;
    oneProtogenesisModal.value.edit(record);
  }

  /**
   * Xóa
   * @param id
   */
  function handleDelete(id) {
    defHttp.delete({ url: Api.delete, data: { ids: id } }, { joinParamsToUrl: true }).then((res) => {
      handleSuccess();
    });
  }

  /**
   * Khởi tạo tùy chọn từ điển
   */
  async function initDictConfig() {
    dictOptions.value['flzds'] = await loadCategoryData({ code: 'B01' });
    dictOptions.value['xiala'] = await initDictOptions('sex');
    dictOptions.value['danxuan'] = await initDictOptions('sex');
    dictOptions.value['duoxuan'] = await initDictOptions('urgent_level');
  }

  /**
   * Sự kiện callback sau khi lưu biểu mẫu
   */
  function handleSuccess() {
    selectedRowKeys.value = [];
    selectionRows.value = [];
    loadData(1);
  }
  onMounted(() => {
    dictOptions.value['kaiguan'] = [
      { text: 'Có', value: '1' },
      { text: 'Không', value: '2' },
    ];
    //Tải trang ban đầu
    loadData();
    //Khởi tạo tùy chọn từ điển
    initDictConfig();
  });
</script>
